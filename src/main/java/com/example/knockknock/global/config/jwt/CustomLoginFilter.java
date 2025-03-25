package com.example.knockknock.global.config.jwt;

import com.example.knockknock.controller.request.UserRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.controller.response.UserResponse;
import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.repository.UserRepository;
import com.example.knockknock.entity.User;
import com.example.knockknock.service.TokenBlacklistService;
import com.example.knockknock.service.TokenListService;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenListService tokenListService;
    private final TokenBlacklistService tokenBlacklistService;
    private static final AntPathRequestMatcher CUSTOM_LOGIN_PATH_MATCHER = new AntPathRequestMatcher("/users/login", "POST");

    // 매직 넘버 상수 정의 - access token 만료 시간: 30분, refresh token 만료 시간: 24시간
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 30*60*1000L;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 24*60*60*1000L;

    public CustomLoginFilter(AuthenticationManager authenticationManager, TokenProvider tokenProvider, UserRepository userRepository,
                             AuthenticationManager authenticationManager1, RedisTemplate<String, String> redisTemplate,
                             TokenListService tokenListService, TokenBlacklistService tokenBlacklistService){
        // 부모 클래스의 생성자를 통해 AuthenticationManager 설정
        super(authenticationManager);
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager1;
        this.redisTemplate = redisTemplate;
        this.tokenListService = tokenListService;
        this.tokenBlacklistService = tokenBlacklistService;
        setRequiresAuthenticationRequestMatcher(CUSTOM_LOGIN_PATH_MATCHER);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 요청 본문(JSON)을 DTO로 변환 후, 잘못된 입력인지 확인
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        UserRequest.LoginRequest loginRequest;

        String userEmail = null;
        String password = null;
        try {
            loginRequest = mapper.readValue(request.getInputStream(), UserRequest.LoginRequest.class);
            userEmail = loginRequest.getEmail();
            password = loginRequest.getPassword();

            log.info("로그인 시도 userEmail = " + maskEmail(userEmail));

            Optional<User> user = userRepository.findByEmail(userEmail);

            // 회원 정보가 존재하는지 확인
            if (user.isEmpty()) {
                ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.NOT_FOUND, "The User does not exist");
                String json = mapper.writeValueAsString(errorResponse);
                response.getWriter().write(json);
                return null;
            }

            // 탈퇴한 회원인지 확인
            if (!user.get().getActive()) {
                ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.INVALID_USER, "Invalid user");
                String json = mapper.writeValueAsString(errorResponse);
                response.getWriter().write(json);
                return null;
            }

            // 이미 발행된 토큰이 존재하는지 확인
            boolean hasExistingToken = tokenListService.isContainToken("RT:RT:" + userEmail);
            if (hasExistingToken) {
                String access = tokenListService.getToken("RT:AT:" + userEmail);
                if (access != null) {
                    tokenBlacklistService.addTokenToList("BL:AT:" + access);
                }
                tokenListService.removeToken("RT:AT:" + userEmail);
                tokenListService.removeToken("RT:RT:" + userEmail);
            }

            log.info("입력된 userEmail = " + userEmail);

            // 탈퇴한 회원이 아니라면 role 은 일단 null로!
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userEmail, password, null);

            log.info("authToken 생성 완료");
            // 데이터가 담긴 토큰을 검증을 위해 AuthenticationManager로 전달함
            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            log.error("Failed to parse login request", e);
            try {
                ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "Failed to parse login request");
                String json = mapper.writeValueAsString(errorResponse);
                response.getWriter().write(json);
            } catch (IOException ex) {
                log.error("Error setting error response", ex);
            }
            return null;
        }
    }

    private String maskEmail(String email){
        if (email == null || email.length() < 4)
            return "***";

        return email.substring(0, 2) + "***" + email.substring(email.length() - 2);
    }


    @Override
    @ResponseBody
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication){
        log.info("로그인 인증 성공 후 로직 실행");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ObjectMapper mapper = new ObjectMapper();

        try{
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String userEmail = customUserDetails.getUserEmail();
            Long userId = customUserDetails.getUserId();
            User user = userRepository.findById(userId).get();
            String nickname = user.getNickname();
            log.info("login nickname = " + nickname);

            // 사용자의 role 데이터 추출
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();

            String role = auth.getAuthority();

            String access = tokenProvider.createJwt("access", userEmail, nickname, role, ACCESS_TOKEN_EXPIRE_TIME);
            String refresh = tokenProvider.createJwt("refresh", userEmail, nickname, role, REFRESH_TOKEN_EXPIRE_TIME);

            try {
                // access token과 refresh Token Redis 저장
                addToken("RT:AT:" + userEmail, access, ACCESS_TOKEN_EXPIRE_TIME);
                addToken("RT:RT:" + userEmail, refresh, REFRESH_TOKEN_EXPIRE_TIME);
            } catch (Exception e) {
                log.error("redis save failure");
                ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "Server Error");
                String json = mapper.writeValueAsString(errorResponse);
                response.getWriter().write(json);
                return; // Redis 저장 실패 시 응답 종료
            }

            UserResponse.LoginResponse loginResponse = UserResponse.LoginResponse.of(user);
            ApiResponse apiSuccessResponse = ApiSuccessResponse.response(ResponseCode.Ok, "User login success!", loginResponse);

            // 응답 설정 및 전송
            response.setHeader("access", access);
            response.addCookie(createCookie("refresh", refresh));
            mapper.writeValue(response.getWriter(), apiSuccessResponse);

        } catch (Exception e){
            log.error("Unexpected error during authentication", e);
            try {
                ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "Server Error");
                String json = mapper.writeValueAsString(errorResponse);
                response.getWriter().write(json);
            } catch (IOException ex) {
                log.error("Error setting error response", ex);
            }
        }
    }

    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);     // 쿠키 생명 주기
        cookie.setSecure(true);        // https 통신을 진행해야 하는 경우
        cookie.setPath("/");         // 쿠키가 적용될 범위
        cookie.setHttpOnly(false);       // true: 클라이언트단에서 javascript로 해당 쿠키에 접근하지 못하게 막아줌

        return cookie;
    }

    private void addToken(String key, String value, Long expiredMs) {
        redisTemplate.opsForValue().set(
                key,            // key 값
                value,          // value
                expiredMs,      // 만료 시간
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed){
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        try {
            ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "Bad request");
            String json = mapper.writeValueAsString(errorResponse);
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error("Error setting error response", e);
        }
    }
}
