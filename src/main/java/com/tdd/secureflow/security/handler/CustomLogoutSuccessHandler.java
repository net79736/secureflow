package com.tdd.secureflow.security.handler;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

import static com.tdd.secureflow.global.util.CookieUtil.removeCookie;
import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.LOGOUT_PATH;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.TOKEN_REISSUE_PATH;
import static org.springframework.http.HttpMethod.POST;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    final JwtProvider jwtProvider;
    final RefreshRepository refreshRepository;

    public CustomLogoutSuccessHandler(JwtProvider jwtProvider, RefreshRepository refreshRepository) {
        this.jwtProvider = jwtProvider;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        logger.info("LogoutSuccessHandler onLogoutSuccess() 실행됨");

        if (!request.getMethod().equals(POST.name())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String refreshToken = extractRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            handleRefreshToken(refreshToken, response);
        }

        clearContextAndRemoveCookies(response);
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        System.out.println("들어옴 4");
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_KEY.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void handleRefreshToken(String refreshToken, HttpServletResponse response) {
        try {
//            if (jwtProvider.isExpired(refreshToken)) {
//                logger.warn("리프레시 토큰이 만료되었습니다.");
//                return;
//            }

//            if (!TOKEN_CATEGORY_REFRESH.equals(jwtProvider.getCategory(refreshToken))) {
//                logger.warn("잘못된 JWT 토큰 형식");
//                return;
//            }

            String email = jwtProvider.getEmail(refreshToken);
//            if (!refreshRepository.existsRefresh(new ExistsRefreshByEmailParam(email))) {
//                logger.warn("인증되지 않은 토큰");
//                return;
//            }


            System.out.println(email);
            System.out.println(email);
            System.out.println(email);
            System.out.println(email);
            System.out.println(email);
            System.out.println(email);
            System.out.println(email);
            System.out.println(email);
            System.out.println(email);
            refreshRepository.deleteRefresh(new DeleteRefreshByEmailParam(email));
            logger.info("리프레시 토큰 삭제 완료");

        } catch (Exception e) {
            logger.error("리프레시 토큰 처리 중 예외 발생", e);
        }
    }

    private void clearContextAndRemoveCookies(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        removeCookie(response, TOKEN_REISSUE_PATH);
        removeCookie(response, LOGOUT_PATH);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
