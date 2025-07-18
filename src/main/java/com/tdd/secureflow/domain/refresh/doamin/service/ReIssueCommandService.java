package com.tdd.secureflow.domain.refresh.doamin.service;

import static com.tdd.secureflow.domain.support.error.ErrorType.INTERNAL_SERVER_ERROR;
import static com.tdd.secureflow.domain.support.error.ErrorType.INVALID_REFRESH_TOKEN;
import static com.tdd.secureflow.domain.support.error.ErrorType.INVALID_TOKEN_TYPE;
import static com.tdd.secureflow.domain.support.error.ErrorType.REFRESH_TOKEN_EXPIRED;
import static com.tdd.secureflow.domain.support.error.ErrorType.REFRESH_TOKEN_NOT_FOUND;
import static com.tdd.secureflow.global.util.CookieUtil.getCookie;
import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_REFRESH;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshByEmailAndRefreshAndExpirationParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.ExistsRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;
import com.tdd.secureflow.domain.refresh.doamin.model.Tokens;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.security.jwt.JwtProvider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReIssueCommandService {

    private final RefreshRepository refreshRepository;
    private final JwtProvider jwtProvider;

    @Value("${jwt.refresh.renewal-threshold-ms:300000}")  // 기본값 5분
    private long refreshRenewalThresholdMs;

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
            // Refresh Token ID 추출
            String refreshTokenId = extractRefreshTokenId(request);
            log.info("Extracted refresh token ID: {}", refreshTokenId);

            // Refresh Token ID로 DB에서 실제 Refresh Token 조회
            Refresh refreshEntity = refreshRepository.findByRefreshTokenId(refreshTokenId);
            if (refreshEntity == null) {
                log.error("Refresh token not found for ID: {}", refreshTokenId);
                throw new CoreException(REFRESH_TOKEN_NOT_FOUND);
            }

            String refresh = refreshEntity.getRefresh();
            log.info("Retrieved refresh token from DB");

            // Refresh Token 검증
            String email = jwtProvider.getEmail(refresh);
            String role = jwtProvider.getRole(refresh);

            log.info("email: {}, role: {}", email, role);

            // 리프레시 토큰 검증
            validateRefreshToken(refresh, email);

            // 새로운 Access 토큰 생성
            String newAccess = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, jwtProvider.getAccessTokenExpiration(), email, role, refreshTokenId);

            // 리프레시 토큰의 남은 유효시간 체크
            boolean shouldRenewRefreshToken = shouldRenewRefreshToken(refresh);
            
            String newRefresh;
            Date expiration;
            
            if (shouldRenewRefreshToken) {
                // 리프레시 토큰이 임계값 이하로 남았을 때만 재발급
                log.info("리프레시 토큰 갱신됨. email: {}", email);
                newRefresh = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, jwtProvider.getRefreshTokenExpiration(), email, role, refreshTokenId);
                expiration = new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpiration().toMillis());
                
                // 기존 리프레시 토큰 삭제 후 새로운 리프레시 토큰 등록
                refreshRepository.deleteRefresh(new DeleteRefreshByEmailParam(email));
                refreshRepository.createRefresh(new CreateRefreshByEmailAndRefreshAndExpirationParam(email, newRefresh, refreshTokenId, expiration));
            } else {
                // 리프레시 토큰은 그대로 유지
                log.info("리프레시 토큰 유지. email: {}", email);
                newRefresh = refresh; // 기존 리프레시 토큰 사용
            }

            return new Tokens(newAccess, refreshTokenId);
        } catch (CoreException e) {
            // CoreException 그대로 던짐
            log.error("CoreException 은 그대로 던짐 : {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("JwtException 은 그대로 던짐 : {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 기타 예외는 CoreException 래핑
            log.error("기타 예외는 CoreException 으로 래핑 : {}", e.getMessage());
            throw new CoreException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 리프레시 토큰의 남은 유효시간을 체크하여 갱신 여부를 결정
     *
     * @param refreshToken 리프레시 토큰
     * @return 갱신이 필요하면 true, 그렇지 않으면 false
     */
    private boolean shouldRenewRefreshToken(String refreshToken) {
        try {
            // 현재 시간
            Instant now = Instant.now();
            
            // 리프레시 토큰의 만료시간 추출
            Date expirationDate = jwtProvider.getExpiration(refreshToken);
            Instant expiration = expirationDate.toInstant();
            
            // 남은 시간 계산
            Duration remainingTime = Duration.between(now, expiration);
            
            // 임계값 설정 (밀리초 단위)
            Duration threshold = Duration.ofMillis(refreshRenewalThresholdMs);
            
            log.debug("현재 시간: {}", now);
            log.debug("만료 시간: {}", expiration);
            log.debug("남은 시간: {}", remainingTime);
            log.debug("임계값: {}", threshold);
            log.debug("비교 결과: {}", remainingTime.compareTo(threshold));
            
            // 리프레시 토큰이 임계값 이하로 남았을 때만 갱신
            return remainingTime.compareTo(threshold) <= 0;
        } catch (Exception e) {
            log.error("리프레시 토큰 갱신 여부 확인 중 오류 발생: {}", e.getMessage());
            // 오류 발생 시 안전하게 갱신하지 않음
            return false;
        }
    }

    /**
     * Refresh Token ID 정보 추출
     *
     * @param request
     * @return
     */
    public String extractRefreshTokenId(HttpServletRequest request) {
        // 쿠키로 부터 리프레시 토큰 ID 추출
        Optional<Cookie> refreshTokenOP = getCookie(request, REFRESH_TOKEN_KEY);
        if (refreshTokenOP.isEmpty()) {
            log.error("Refresh token ID cookie is empty.");
            throw new CoreException(REFRESH_TOKEN_NOT_FOUND);
        }

        // 쿠키에서 refreshTokenId 추출
        return refreshTokenOP.get().getValue();
    }

    /**
     * Refresh Token 정보 추출 (기존 메서드 - 호환성을 위해 유지)
     *
     * @param request
     * @return
     */
    public String extractRefreshToken(HttpServletRequest request) {
        // 쿠키로 부터 리프레시 토큰 추출
        Optional<Cookie> refreshTokenOP = getCookie(request, REFRESH_TOKEN_KEY);
        if (refreshTokenOP.isEmpty()) {
            log.error("Refresh token cookie is empty.");
            throw new CoreException(REFRESH_TOKEN_NOT_FOUND);
        }

        // 리프레시 토큰에서 액세스 토큰 추출
        return refreshTokenOP.get().getValue();
    }

}
