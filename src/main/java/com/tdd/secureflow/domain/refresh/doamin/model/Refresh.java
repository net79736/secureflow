package com.tdd.secureflow.domain.refresh.doamin.model;

import java.time.Instant;

import com.tdd.secureflow.domain.common.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refreshes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refresh extends BaseEntity {
    @Column(nullable = false)
    private String email;
    @Column(nullable = false, columnDefinition = "BLOB")
    private String refresh;
    @Column(nullable = false, columnDefinition = "BLOB")
    private String refreshTokenId;
    @Column(nullable = false)
    private Instant expiration; // 토큰의 정상 만료 시각

    // {@code true}면 로그아웃·재로그인 등으로 무효화됨. 행은 이력/감사용으로 유지.
    @Column(nullable = false)
    private boolean revoked;

    // 토큰을 무효화한 시각. {@code revoked == false}이면 null
    private Instant revokedAt;

    @Builder
    public Refresh(String email, String refresh, String refreshTokenId, Instant expiration) {
        this.email = email;
        this.refresh = refresh;
        this.refreshTokenId = refreshTokenId;
        this.expiration = expiration;
        this.revoked = false;
        this.revokedAt = null;
    }
}