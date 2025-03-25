package com.example.knockknock.service;

import com.example.knockknock.controller.request.UserRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.Role;
import com.example.knockknock.entity.User;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.global.config.jwt.TokenProvider;
import com.example.knockknock.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenListService tokenListService;
    private final TokenBlacklistService tokenBlacklistService;

    // 회원가입
    public ApiResponse join(UserRequest.JoinRequest joinRequest) {
        String userEmail = joinRequest.getEmail();
        String password = joinRequest.getPassword();
        String nickname = joinRequest.getNickname();
        Date birth = joinRequest.getBirth();

        log.info("joinProcess(), userEmail = " + userEmail);

        User data = User.builder()
                .email(userEmail)
                .password(bCryptPasswordEncoder.encode(password))
                .nickname(nickname)
                .birth(birth)
                .role(Role.USER)
                .active(true)
                .build();

        userRepository.save(data);

        return ApiSuccessResponse.response(ResponseCode.Created, "Join request success!", null);
    }

    @Transactional
    public ApiResponse withdraw(Long userId, HttpServletRequest request, HttpServletResponse response){
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ObjectMapper mapper = new ObjectMapper();

        try {
            User userEntity = userRepository.findById(userId).get();
            String userEmail = userEntity.getEmail();

            // get refresh token
            String refresh = null;
            Cookie[] cookies = request.getCookies();
            log.info("in UserServiceImpl, cookies = " + cookies);
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }

            // refresh null check
            if (refresh == null) {
                return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Refresh token does not exist");
            }

            // expired check
            try {
                tokenProvider.isExpired(refresh);
            } catch (ExpiredJwtException e) {
                return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "The token is expired");
            }

            // 토큰이 refresh인지 확인
            String category = tokenProvider.getCategory(refresh);
            if (!category.equals("refresh")) {
                return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token category");
            }

            // BlackList에 Access token이 저장되어 있는지 확인하고
            String access = request.getHeader("access");
            boolean isBlacked = tokenBlacklistService.isContainToken("BL:AT:" + access);
            if (isBlacked) {
                return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token");
            }

            // Redis에 Refresh Token이 저장되어 있는지 확인
            boolean isExist = tokenListService.isContainToken("RT:RT:" + userEmail);
            log.info("회원 탈퇴 요청, isExist = " + isExist);
            if(!isExist){
                ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token");
                String json = mapper.writeValueAsString(errorResponse);
                response.getWriter().write(json);
                return null;
            }

            tokenListService.removeToken("RT:RT:" + userEmail);
            tokenBlacklistService.addTokenToList("BL:AT:" + access);

            // 회원 상태(status) false 전환

            userEntity.setActive(false);
            log.info("in UserServiceImpl, userId = " + userId);

            // Refresh Token Cookie 값 0
            Cookie cookie = new Cookie("refresh", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");

            ApiResponse apiResponse = ApiSuccessResponse.response(ResponseCode.Ok, "Withdraw request success!", null);
            String json = mapper.writeValueAsString(apiResponse);
            response.getWriter().write(json);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token");
        }
        // @Transactional 어노테이션을 사용하여 JPA가 트랜잭션 내에서 자동으로 변경 사항을 flush하여 DB에 반영함
    }

    public ApiResponse checkEmail(String email){
        Optional<User> userEntity = userRepository.findByEmail(email);

        // 동일한 이메일이 존재한다면,
        if (!userEntity.isEmpty()){
            Map<String, String> data = new HashMap<>();
            data.put("email", email);
            return ApiSuccessResponse.response(ResponseCode.Ok, "사용할 수 없는 이메일입니다.", data);
        }

        // 동일한 이메일이 존재하지 않는다면,
        return ApiSuccessResponse.response(ResponseCode.Ok, "사용할 수 있는 이메일입니다.",null);
    }
}
