package com.tdd.secureflow.security.jwt.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdd.secureflow.interfaces.api.support.ErrorResponse;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("토큰에 문제가 있습니다.");
            log.error("jwtException getMessage() : {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            //  `ErrorResponse` 객체 생성 후 JSON 변환하여 응답
            ErrorResponse<Void> errorResponse = new ErrorResponse<>(
                    HttpStatus.UNAUTHORIZED.name(),  // "UNAUTHORIZED"
                    e.getMessage(),
                    null  // data 필드는 사용하지 않음
            );

            objectMapper.writeValue(response.getWriter(), errorResponse);
            // response.getWriter().write(String.format("{\"code\":\"%s\", \"message\":\"%s\"}", HttpStatus.UNAUTHORIZED.name(), e.getMessage()));
        }
    }
}