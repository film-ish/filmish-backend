package com.example.knockknock.global.config.jwt;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.entity.User;
import com.example.knockknock.repository.UserRepository;
import com.example.knockknock.service.TokenBlacklistService;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 access 키에 담긴 토큰을 꺼냄
        String accessToken = request.getHeader("access");
        log.info("JWTFilter에서 accessToken = " + accessToken);

        // OAuth2 관련 경로는 JWT 검증 건너뛰기
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/users/login/social") ||
                requestURI.startsWith("/login/oauth2/code/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null || accessToken.isEmpty()){
            log.info("access token is null");
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ObjectMapper mapper = new ObjectMapper();

        // 토큰이 있다면
        // 1. blackList에 존재하는지 확인
        boolean isBlacked = tokenBlacklistService.isContainToken("BL:AT:" + accessToken);
        if(isBlacked) {
            log.info("The token is blacked.");
            ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token");
            String json = mapper.writeValueAsString(errorResponse);
            response.getWriter().write(json);
            return;
        }

        // 2. 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            tokenProvider.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            log.info("Token is expired.");
            ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Token is expired");
            String json = mapper.writeValueAsString(errorResponse);
            response.getWriter().write(json);
            return;
        }

        // userEmail, role 값 획득
        String userEmail = tokenProvider.getUserEmail(accessToken);

        log.info("userEmail = " + userEmail);
        log.info("token category = " + tokenProvider.getCategory(accessToken));

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if(userOptional.isEmpty()){
            log.info("User is empty.");
            ApiErrorResponse errorResponse = ApiErrorResponse.of(ErrorCode.NOT_FOUND, "The user doesn't exist");
            String json = mapper.writeValueAsString(errorResponse);
            response.getWriter().write(json);
            return;
        }

        User userEntity = userOptional.get();
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        // Spring Security 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        // SecurityContextHolder에게 넘기면 일시적으로 세션이 생성됨 - 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 검증 종료 후 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }
}
