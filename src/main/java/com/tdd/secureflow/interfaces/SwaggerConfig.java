package com.tdd.secureflow.interfaces;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;
import static com.tdd.secureflow.interfaces.CommonSecurityScheme.JWT_FORMAT;

@Configuration
public class SwaggerConfig {
    /**
     * OpenAPI 설정을 커스터마이징하는 Bean 을 정의
     * 이 메서드는 Swagger UI 에서 API 보안을 구성하기 위해 사용된다.
     * 'Authorization' 헤더를 통한 JWT 기반 인증 방식을 추가합니다.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Swagger UI 에서 보안 요구 사항을 추가한다.
                .addSecurityItem(
                        new SecurityRequirement().addList(CommonHttpHeader.HEADER_AUTHORIZATION)) // 'Authorization' 보안 스키마를 요구사항에 추가
                // 'Authorization' 이라는 이름의 보안 스키마를 정의한다.
                .components(new Components()
                        .addSecuritySchemes(CommonHttpHeader.HEADER_AUTHORIZATION, createJWTTokenScheme()))
                .info(apiInfo());
    }

    private SecurityScheme createJWTTokenScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // HTTP 인증 방식을 사용
                .scheme(BEARER_SCHEME)
                .bearerFormat(JWT_FORMAT)
                .in(SecurityScheme.In.HEADER) // 인증 값은 HTTP 헤더에서 전달됨
                .name(CommonHttpHeader.HEADER_AUTHORIZATION); // 헤더 이름은 'Authorization' 으로 지정
    }

    private Info apiInfo() {
        return new Info()
                .title("SecureFlow API")
                .description("SecureFlow API Documentation")
                .version("1.0.0");
    }
}
