package com.tdd.secureflow.infra.db.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;

public interface RefreshJpaRepository extends JpaRepository<Refresh, Long> {
    Boolean existsByEmail(String email);

    void deleteByEmail(String email);

    Refresh findByRefreshTokenId(String refreshTokenId);
}
