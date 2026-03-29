package com.tdd.secureflow.infra.db.jwt;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;

public interface RefreshJpaRepository extends JpaRepository<Refresh, Long> {

    // 해당 이메일의 아직 무효화되지 않은 리프레시 행이 존재하는지 확인
    Boolean existsByEmailAndRevokedFalse(String email);

    // 해당 refreshTokenId의 아직 무효화되지 않은 리프레시 행을 조회
    Refresh findByRefreshTokenIdAndRevokedFalse(String refreshTokenId);

    // 해당 이메일의 아직 무효화되지 않은 리프레시 행을 소프트 삭제(무효화)합니다.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Refresh r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.email = :email AND r.revoked = false")
    int revokeByEmail(@Param("email") String email, @Param("revokedAt") Instant revokedAt);

    // 만료 시각이 cutoff보다 이전인 행을 물리 삭제합니다. 배치 정리용.
    long deleteByExpirationBefore(Instant cutoff);
}
