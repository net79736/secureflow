package com.tdd.secureflow.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;
import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;

@Component
@RequiredArgsConstructor
public class JwtResponseHandler {

    private final JwtProvider jwtProvider;

    public void addAuthorizationHeader(HttpServletResponse response, String email, String role) {
        // Authorization
        System.out.println("aaaaaaaaaaaaaaaaaa");
        System.out.println("aaaaaaaaaaaaaaaaaa");
        System.out.println("aaaaaaaaaaaaaaaaaa");
        System.out.println("aaaaaaaaaaaaaaaaaa");
        System.out.println("email = " + email);
        System.out.println("role = " + role);
        String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofDays(1), email, role);
        // 응답 헤더 설정
        response.addHeader(HEADER_AUTHORIZATION, String.format("%s %s", BEARER_SCHEME, accessToken));
    }


}
