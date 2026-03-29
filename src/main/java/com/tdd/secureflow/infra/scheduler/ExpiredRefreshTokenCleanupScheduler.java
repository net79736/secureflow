package com.tdd.secureflow.infra.scheduler;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DB에 남아 있는 <strong>만료된</strong> 리프레시 토큰 행을 주기적으로 삭제합니다.
 * <p>
 * 로그아웃·재발급 시에는 {@code deleteRefresh}(소프트 무효화)로 {@code revoked} 처리되지만,
 * 만료 시각이 지난 행은 배치에서 물리 삭제해 테이블 크기를 관리합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "secureflow.refresh-token-cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ExpiredRefreshTokenCleanupScheduler {

    private final RefreshRepository refreshRepository;

    @Scheduled(cron = "${secureflow.refresh-token-cleanup.cron:0 0 3 * * *}")
    public void deleteExpiredRefreshTokens() {
        Instant now = Instant.now();
        long removed = refreshRepository.deleteExpiredBefore(now);
        if (removed > 0) {
            log.info("Expired refresh token cleanup: removed {} row(s), cutoff={}", removed, now);
        } else {
            log.debug("Expired refresh token cleanup: no rows to remove, cutoff={}", now);
        }
    }
}
