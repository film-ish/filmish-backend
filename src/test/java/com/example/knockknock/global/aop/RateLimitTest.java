package com.example.knockknock.global.aop;

import com.example.knockknock.KnockKnockApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

@Import(RateLimitTest.TestConfig.class)
@SpringBootTest(classes = KnockKnockApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RateLimitTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        // 테스트 전 캐시 초기화
        rateLimitInterceptor.clearCache();
    }

    @Test
    public void testRateLimiting() {
        // Given: 테스트할 API 엔드포인트와 요청 횟수 설정
        String url = "http://localhost:" + port + "/api/v1/";
        int totalRequests = 15;

        // 모든 요청에 동일한 IP 사용
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-FORWARDED-FOR", "127.0.0.1");

        int successCount = 0; // 성공한 요청 수
        int failCount = 0;    // 실패한 요청 수

        // When: API 엔드포인트에 반복적으로 요청을 보냄
        for (int i = 0; i < totalRequests; i++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        org.springframework.http.HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    successCount++;
                }
            } catch (HttpClientErrorException.TooManyRequests tooManyRequests) {
                failCount++;
            }
        }

        // Then: 속도 제한 규칙이 올바르게 적용되었는지 검증
        assertThat(successCount).isEqualTo(5); // 성공한 요청은 최대 5개여야 함
        assertThat(failCount).isEqualTo(10);   // 실패한 요청은 나머지여야 함
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public WebMvcConfigurer webMvcConfigurer(RateLimitInterceptor interceptor) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    registry.addInterceptor(interceptor).addPathPatterns("/**");
                }
            };
        }
    }
}
