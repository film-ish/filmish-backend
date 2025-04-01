package com.example.knockknock.controller;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.global.config.jwt.TokenProvider;
import com.example.knockknock.service.TokenListService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ReissueController", description = "회원 관련 토큰 재발행 컨트롤러")
public class ReissueController {
    private final TokenProvider tokenProvider;
    private final TokenListService tokenListService;
    private final RedisTemplate<String, String> redisTemplate;

    // 매직 넘버 상수 정의
    // access token 만료 시간: 30분, refresh token 만료 시간: 24시간
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 30*60*1000L;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 24*60*60*1000L;

    @GetMapping("/users/reissue")
    @Operation(summary = "Access token 재발행", description = "refresh 토큰을 기반으로 access 토큰을 재발행합니다.",
            security = {@SecurityRequirement(name = "access"), @SecurityRequirement(name = "refresh")})
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발행 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "refresh 토큰이 존재하지 않거나 유효하지 않음")
    })
    public ApiResponse reissue(HttpServletRequest request, HttpServletResponse response){
        // get tokens
        String access = request.getHeader("access");
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        for (Cookie cookie: cookies) {
            if(cookie.getName().equals("refresh")){
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "refresh token is null");
        }

        // expired check
        try {
            tokenProvider.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "refresh token is expired");
        }

        // Token이 refresh인지 확인 (발급시 페이로드에 명시함)
        String category = tokenProvider.getCategory(refresh);
        if (!category.equals("refresh")) {
            return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "invalid token");
        }

        // 토큰 만료 여부 확인
        try {
            tokenProvider.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            log.info("The refresh token is expired.");
            return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Refresh token is expired");
        }

        // Access token이 Blacklist에 있는지 확인
        Boolean isExist = tokenListService.isContainToken("BL:RT:" + access);
        if (isExist){
            return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "invalid token");
        }

        String userEmail = tokenProvider.getUserEmail(refresh);
        String nickname = tokenProvider.getNickname(refresh);
        String role = tokenProvider.getRole(refresh);

        // make new JWT
        String newAccess = tokenProvider.createJwt("access", userEmail, nickname, role, ACCESS_TOKEN_EXPIRE_TIME);
        String newRefresh = tokenProvider.createJwt("refresh", userEmail, nickname, role, REFRESH_TOKEN_EXPIRE_TIME);

        addToken("RT:AT:" + userEmail, newAccess, ACCESS_TOKEN_EXPIRE_TIME);
        addToken("RT:RT:" + userEmail, newRefresh, REFRESH_TOKEN_EXPIRE_TIME);

        // response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return ApiSuccessResponse.response(ResponseCode.Ok, "Access token has successfully reissued." , null);
    }

    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(false);

        return cookie;
    }

    private void addToken(String key, String value, Long expiredMs) {
        redisTemplate.opsForValue().set(
                key,       // key 값
                value,                // value
                System.currentTimeMillis() + expiredMs,     // 만료 시간
                TimeUnit.MILLISECONDS
        );
    }
}
