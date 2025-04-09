package com.example.knockknock.global.aop;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // 테스트를 위한 캐시 초기화 메서드 추가
    public void clearCache() {
        this.cache.clear();
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);

        // 버킷 가져오기(생성)
        Bucket bucket = cache.computeIfAbsent(clientIp, key ->
                Bucket.builder()
                        .addLimit(limit -> limit
                                .capacity(5)
                                .refillGreedy(5, Duration.ofMinutes(30))
                        )
                        .build()
        );

        // 토큰 소비 시도
        if (bucket.tryConsume(1)) {
            return true; // 요청 처리 계속 진행
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please try again later.");

            return false; // 요청 처리 중단
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
    }
}
