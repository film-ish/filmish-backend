package com.example.knockknock.global.config.security;

import com.example.knockknock.global.config.jwt.CustomLoginFilter;
import com.example.knockknock.global.config.jwt.CustomLogoutFilter;
import com.example.knockknock.global.config.jwt.JwtFilter;
import com.example.knockknock.global.config.jwt.TokenProvider;
import com.example.knockknock.repository.UserRepository;
import com.example.knockknock.service.TokenBlacklistService;
import com.example.knockknock.service.TokenListService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenListService tokenListService;
    private final TokenBlacklistService tokenBlacklistService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenProvider tokenProvider,
                                           AuthenticationConfiguration authenticationConfiguration) throws Exception {
        try{
            log.info("Configuring security filter chain");

            http
                    .authorizeHttpRequests((auth) -> auth
                            // swagger UI 관련 경로 허용
                            .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
                            .requestMatchers(HttpMethod.GET, "/swagger-resources/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/webjars/**").permitAll()
                            .requestMatchers("/api/v1/knockknock").permitAll()
                            .requestMatchers("/knockknock", "/upload-json", "/knockknock/movies", "/knockknock/directors",
                                                "/knockknock/actors", "/knockknock/genres", "/knockknock/keywords").permitAll()
                            .requestMatchers("/", "/knockknock","/users", "/users?**", "/users/login", "/users/reissue", "/movies/genre/{genreId}",
                                    "/movies/{movieId}", "/movies/{movieId}/reviews", "/reviews/{reviewId}", "/reviews/{reviewId}/comments",
                                    "/reviews/comments", "/makers/{makerId}", "/qna/{makerId}", "/qna/{qnaId}/comments", "/cinemas",
                                    "/platforms", "/actors", "/recommendation/genres", "/recommendation/rates").permitAll()
                            .requestMatchers("/movies/**").authenticated()

                            .anyRequest().authenticated()
                    )
                    .exceptionHandling(exception -> exception
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                System.out.println("Requested URL: " + request.getRequestURI());
                                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                            })
                    );
            log.debug("Authorization rules configured successfully");


            http
                    .headers(headers -> headers
                            .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                            .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin-allow-popups"))
                    );

            http
                    .httpBasic((auth) -> auth.disable());

            http
                    .csrf((auth) -> auth.disable());

            http
                    .formLogin((auth) -> auth.disable());

            // cors 설정
            http
                    .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                        @Override
                        public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                            CorsConfiguration configuration = new CorsConfiguration();

                            configuration.setAllowedOriginPatterns(Collections.singletonList("*")); // Websocket 때문에 바꾼 설정
                            configuration.setAllowedMethods(Collections.singletonList("*"));
                            configuration.setAllowCredentials(true);        // 프론트에서 credential 설정하면 true로 설정해줘야 함
                            configuration.setAllowedHeaders(Collections.singletonList("*"));
                            configuration.setMaxAge(3600L);
                            configuration.setExposedHeaders(Arrays.asList("Authorization", "access", "userId", "email"));
                            return configuration;
                        }
                    })));

            AuthenticationManager authManager = authenticationManager(authenticationConfiguration);

            http
                    .addFilterAfter(new JwtFilter(tokenProvider, userRepository, tokenBlacklistService),
                            CustomLoginFilter.class);

            // CustomLogoutFilter 등록
            http
                    .addFilterBefore(new CustomLogoutFilter(tokenProvider, tokenListService, tokenBlacklistService), LogoutFilter.class);

            //필터 추가 LoginFilter()는 인자를 받음
            // AuthenticationManager()와 JWTUtil 인수 전달
            http
                    .addFilterAt(new CustomLoginFilter(authenticationManager(authenticationConfiguration),
                            tokenProvider,
                            userRepository,
                            authManager,
                            redisTemplate,
                            tokenListService,
                            tokenBlacklistService
                    ),UsernamePasswordAuthenticationFilter.class);

            http
                    .sessionManagement((session) -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

            log.info("Security configuration completed successfully");
            return http.build();
        } catch (Exception e) {
            log.error("Error configuring security: ", e);
            throw e;
        }
    }
}
