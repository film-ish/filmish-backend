package com.example.knockknock.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SwaggerConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnProperty(name = "springdoc.show-login-endpoint", havingValue = "true")
    public OpenApiCustomizer springSecurityLoginEndpointCustomizer() {
        return OpenApi -> {
            OpenApi.getPaths().addPathItem("/users/login", new PathItem()
                    .post(new Operation()
                            .summary("로그인")
                            .description("JWT 로그인 요청 엔드포인트입니다.")
                            .requestBody(new RequestBody()
                                    .required(true)
                                    .content(new Content()
                                    .addMediaType("application/json", new MediaType().schema(new Schema<>()
                                            .addProperty("email", new StringSchema())
                                            .addProperty("password", new StringSchema())))))
                            .responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("OK")))
                    )
            );
        };
    }

    @Bean
    @ConditionalOnProperty(name = "springdoc.show-login-endpoint", havingValue = "true")
    public OpenApiCustomizer springSecurityLogoutEndpointCustomizer() {
        return OpenApi -> {
            OpenApi.getPaths().addPathItem("/users/logout", new PathItem()
                    .post(new Operation()
                            .summary("로그아웃")
                            .description("JWT 로그아웃 요청 엔드포인트입니다.")
                            .responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("OK")))
                    )
            );
        };
    }

    @Bean
    public GroupedOpenApi publicApi( @Qualifier("springSecurityLoginEndpointCustomizer") OpenApiCustomizer springSecurityLoginEndpointCustomizer,
                                     @Qualifier("springSecurityLogoutEndpointCustomizer") OpenApiCustomizer springSecurityLogoutEndpointCustomizer) {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(springSecurityLoginEndpointCustomizer)
                .addOpenApiCustomizer(springSecurityLogoutEndpointCustomizer)
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("access")
                .description("JWT token");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("access-key", securityScheme))
                .info(new Info()
                        .title("똑똑 API")
                        .description("독립 영화 추천 웹 애플리케이션의 데이터 CRUD를 위한 API입니다.")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("access-key"));
    }
}
