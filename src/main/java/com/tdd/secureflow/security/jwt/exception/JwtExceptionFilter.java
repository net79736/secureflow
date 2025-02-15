package com.tdd.secureflow.security.jwt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdd.secureflow.interfaces.api.support.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            System.out.println("토큰이 만료되어 실행 되었습니다.");
            response.setStatus(410);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            //  `ErrorResponse` 객체 생성 후 JSON 변환하여 응답
            ErrorResponse<Void> errorResponse = new ErrorResponse<>(
                    HttpStatus.GONE.name(),  // "GONE"
                    e.getMessage(),
                    null  // data 필드는 사용하지 않음
            );

            objectMapper.writeValue(response.getWriter(), errorResponse);
            // response.getWriter().write(String.format("{\"code\":\"%s\", \"message\":\"%s\"}", HttpStatus.GONE.name(), e.getMessage()));
        }
    }
}