package com.tdd.secureflow.domain.refresh.doamin.service;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.ExistsRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.model.Tokens;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.domain.support.error.ErrorType;
import com.tdd.secureflow.security.jwt.JwtProvider;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

import static com.tdd.secureflow.domain.support.error.ErrorType.*;
import static com.tdd.secureflow.global.util.CookieUtil.getCookie;
import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_REFRESH;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReIssueCommandService {

    private final RefreshRepository refreshRepository;
    private final JwtProvider jwtProvider;

    /**
     * Refresh Token 검증
     */
    public void validateRefreshToken(String refresh, String email) {
        log.info("refresh : {}, email: {}", refresh, email);

        // 리프레시 토큰 만료 여부 체크
        try {
            Boolean expired = jwtProvider.isExpired(refresh);
            if (expired) {
                throw new CoreException(REFRESH_TOKEN_EXPIRED);
            }
        } catch (ExpiredJwtException e) {
            throw new CoreException(INVALID_REFRESH_TOKEN);
        }

        // 리프레시 토큰 카테고리 검증
        String category = jwtProvider.getCategory(refresh);
        if (!category.equals(TOKEN_CATEGORY_REFRESH)) {
            throw new CoreException(INVALID_TOKEN_TYPE);
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsRefresh(new ExistsRefreshByEmailParam(email));
        if (!isExist) {
            log.info("기존의 리프레시 토큰이 존재하지 않음");
            throw new CoreException(REFRESH_TOKEN_NOT_FOUND);
        }
    }

    public Tokens reissueTokens(HttpServletRequest request) {
        try {
            // Refresh Token 추출 및 디코딩
            String refresh = extractRefreshToken(request);

            log.info("Extracted refresh token: {}", refresh);

            // Refresh Token 검증
            String email = jwtProvider.getEmail(refresh);
            String role = jwtProvider.getRole(refresh);

            log.info("email: {}, role: {}", email, role);

            validateRefreshToken(refresh, email);

            // 새로운 Access 및 Refresh 토큰 생성
            // Authorization
            String newAccess = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofSeconds(30), email, role);
            // X-Refresh-Token
            String newRefresh = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofHours(24), email, role);

            log.info("쿠키 확인용 refresh: {}, publicId: {}", refresh, email);
            log.info("쿠키 확인용 refresh: {}, publicId: {}", refresh, email);

            // 기존 리프레시 토큰 삭제
            refreshRepository.deleteRefresh(new DeleteRefreshParam(email));
            // 새로운 리프레시 토큰 등록
            Date expiration = new Date(System.currentTimeMillis() + Duration.ofHours(24).toMillis());
            refreshRepository.createRefresh(new CreateRefreshParam(email, refresh, expiration));

            return new Tokens(newAccess, newRefresh);
        } catch (CoreException e) {
            // CoreException 그대로 던짐
            log.error("PlayHiveException 은 그대로 던짐 : {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 기타 예외는 CoreException 래핑
            log.error("기타 예외는 PlayHiveException 으로 래핑 : {}", e.getMessage());
            throw new CoreException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Refresh Token 정보 추출
     *
     * @param request
     * @return
     */
    public String extractRefreshToken(HttpServletRequest request) {
        // 쿠키로 부터 리프레시 토큰 추출
        Optional<Cookie> refreshTokenOP = getCookie(request, REFRESH_TOKEN_KEY);
        if (refreshTokenOP.isEmpty()) {
            log.error("Refresh token cookie is empty.");
            throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
        }

        // URL 디코딩된 리프레시 토큰 값
        String rawToken = refreshTokenOP.get().getValue();
        String refresh = URLDecoder.decode(rawToken, StandardCharsets.UTF_8);
        log.debug("Decoded refresh token: {}", refresh);

        if (refresh == null || refresh.isBlank()) {
            log.error("Decoded refresh token is blank.");
            throw new CoreException(INVALID_REFRESH_TOKEN);
        }

        // 리프레시 토큰에서 액세스 토큰 추출
        return refresh;
    }

}
