package com.tdd.secureflow.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("소셜 로그인 실패! 서버 로그를 확인해주세요.");

        response.setContentType("text/html; charset=UTF-8"); // 응답 Content-Type 설정
        response.setCharacterEncoding("UTF-8"); // 응답 인코딩 설정
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

//        if (exception instanceof ExistingUserAuthenticationException) {
//            response.getWriter().write("{\"error\": \"USER_ALREADY_EXISTS\", \"message\": \"" + exception.getMessage() + "\"}");
//        } else {
//            response.getWriter().write("{\"error\": \"OAUTH2_UNAUTHORIZED\", \"message\": \"" + exception.getMessage() + "\"}");
//        }

        String frontOrigin = "http://localhost:8082";  // 실제 프론트엔드 URL로 설정
        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8).replace("+", "%20"); // `+`를 `%20`으로 변환
        String script = String.format(
                "<script>" +
                        "window.opener.postMessage({ message: '%s', status: 'fail' }, '%s');" +
                        "window.close();" +  // 메시지 전송 후 팝업 닫기
                        "</script>", errorMessage, frontOrigin
        );

        response.getWriter().write(script);

        log.info("소셜 로그인에 실패했습니다. 에러 메시지 : {}", exception.getMessage());
    }
}