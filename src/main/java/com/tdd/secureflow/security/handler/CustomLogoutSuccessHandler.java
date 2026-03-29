package com.tdd.secureflow.security.handler;

import static com.tdd.secureflow.global.util.CookieUtil.removeCookie;
import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.LOGOUT_PATH;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.TOKEN_REISSUE_PATH;
import static org.springframework.http.HttpMethod.POST;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.tdd.secureflow.domain.loginhistory.service.LoginHistoryService;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.security.jwt.JwtProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    final JwtProvider jwtProvider;
    final RefreshRepository refreshRepository;
    final LoginHistoryService loginHistoryService;

    public CustomLogoutSuccessHandler(
            JwtProvider jwtProvider,
            RefreshRepository refreshRepository,
            LoginHistoryService loginHistoryService
    ) {
        this.jwtProvider = jwtProvider;
        this.refreshRepository = refreshRepository;
        this.loginHistoryService = loginHistoryService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        logger.info("LogoutSuccessHandler onLogoutSuccess() 실행됨");

        if (!request.getMethod().equals(POST.name())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String refreshTokenId = extractRefreshTokenIdFromCookies(request);
        if (refreshTokenId != null) {
            handleRefreshToken(refreshTokenId, response);
        }

        clearContextAndRemoveCookies(response);
    }

    private String extractRefreshTokenIdFromCookies(HttpServletRequest request) {
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

    private void handleRefreshToken(String refreshTokenId, HttpServletResponse response) {
        try {
            // refreshTokenId로 DB에서 실제 refreshToken 조회
            Refresh refreshEntity = refreshRepository.findByRefreshTokenIdAndRevokedFalse(refreshTokenId);
            if (refreshEntity == null) {
                logger.warn("Refresh token not found for ID: {}", refreshTokenId);
                return;
            }

            String refreshToken = refreshEntity.getRefresh();
            String email = jwtProvider.getEmail(refreshToken);

            loginHistoryService.closeLatestOpenSession(email); // 가장 최근의 미종료 성공 세션에 종료 시각을 기록합니다.
            refreshRepository.revokeByEmail(new DeleteRefreshByEmailParam(email));
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
