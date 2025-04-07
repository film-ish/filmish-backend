package com.example.knockknock.global.config.jwt;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.service.TokenBlacklistService;
import com.example.knockknock.service.TokenListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {
    private final TokenProvider tokenProvider;
    private final TokenListService tokenListService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // path and method verify
        String requestUri = request.getRequestURI();
        ObjectMapper objectMapper = new ObjectMapper();

        // 로그아웃 관련 URI와 Http Method 설정
        if (!requestUri.matches("^\\/api\\/v1\\/users\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
                log.info("in CustomLogoutFilter, refresh token = " + refresh);
                log.info("Received cookie - name: " + cookie.getName()
                        + ", value: " + cookie.getValue()
                        + ", domain: " + cookie.getDomain()
                        + ", path: " + cookie.getPath()
                        + ", maxAge: " + cookie.getMaxAge()
                        + ", secure: " + cookie.getSecure()
                        + ", httpOnly: " + cookie.isHttpOnly());
            }
        }

        // refresh null check
        if(refresh == null) {
            response.setStatus(HttpServletResponse.SC_OK);
            ApiResponse apiErrorResponse = ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Not included the refresh token");
            objectMapper.writeValue(response.getWriter(), apiErrorResponse);
            return;
        }

        // expired check
        try {
            tokenProvider.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_OK);
            ApiResponse apiErrorResponse = ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Expired token.");
            objectMapper.writeValue(response.getWriter(), apiErrorResponse);
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = tokenProvider.getCategory(refresh);
        if (!category.equals("refresh")) {
            response.setStatus(HttpServletResponse.SC_OK);
            ApiResponse apiErrorResponse = ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Not included the refresh token");
            objectMapper.writeValue(response.getWriter(), apiErrorResponse);
            return;
        }

        // blackList 확인
        String access = request.getHeader("access");
        log.info("access 토큰 : {}", access);
        boolean isBlacked = tokenBlacklistService.isContainToken("BL:AT:" + access);
        log.info("isBlacked = {}", isBlacked);
        if(isBlacked) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            ApiResponse apiErrorResponse = ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token. Need to logged in.");
            objectMapper.writeValue(response.getWriter(), apiErrorResponse);
            return;
        }

        String userEmail = tokenProvider.getUserEmail(refresh);

        // 로그아웃 진행
        // refresh token을 redis에서 제거
        tokenListService.removeToken("RT:RT:" + userEmail);

        // access token을 blackList에 추가
        tokenBlacklistService.addTokenToList("BL:AT:" + access);
        tokenListService.removeToken("RT:AT:" + userEmail);

        // Refresh Token Cookie 값 0 설정
        Cookie cookie = new Cookie("refresh", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");             // 로그인 시와 동일하게 설정
        cookie.setHttpOnly(false);
        cookie.setSecure(true);

        // SecurityContext 클리어
        SecurityContextHolder.clearContext();

        // 응답 설정 및 전송
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.addCookie(cookie);

        response.setStatus(HttpServletResponse.SC_OK);
        ApiResponse apiSuccessResponse1 = ApiSuccessResponse.response(ResponseCode.Ok, "Successfully logged out.", null);
        objectMapper.writeValue(response.getWriter(), apiSuccessResponse1);
    }
}
