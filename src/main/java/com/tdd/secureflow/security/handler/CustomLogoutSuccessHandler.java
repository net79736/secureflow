package com.tdd.secureflow.security.handler;

import com.tdd.secureflow.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static org.springframework.http.HttpMethod.POST;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    final JwtProvider jwtProvider;

    public CustomLogoutSuccessHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("LogoutSuccessHandler onLogoutSuccess() 메서드를 실행하였습니다");

        String method = request.getMethod();
        if (!method.equals(POST.name())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(REFRESH_TOKEN_KEY)) {
                    refresh = cookie.getValue();
                }
            }
        }

        // 1. Security Context 해제 및 빈 쿠키 추가
        clearContextAndAddCookie(response);
    }

    /**
     * SecurityContext 초기화 및 빈 쿠키 추가 메서드
     *
     * @param response HttpServletResponse
     */
    private void clearContextAndAddCookie(HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        Cookie cookie = new Cookie(REFRESH_TOKEN_KEY, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
