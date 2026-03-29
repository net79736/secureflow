package com.tdd.secureflow.infra.db.jwt.impl;

import java.time.Instant;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshByEmailAndRefreshAndExpirationParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.ExistsRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.infra.db.jwt.RefreshJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshRepositoryImpl implements RefreshRepository {

    private final RefreshJpaRepository refreshJpaRepository;

    @Override
    public Boolean existsByEmailAndRevokedFalse(ExistsRefreshByEmailParam param) {
        // 해당 이메일의 아직 무효화되지 않은 리프레시 행이 존재하는지 확인
        return refreshJpaRepository.existsByEmailAndRevokedFalse(param.email());
    }

    @Override
    @Transactional
    public Refresh createRefresh(CreateRefreshByEmailAndRefreshAndExpirationParam param) {
        Refresh refreshEntity = Refresh.builder()
                .email(param.email())
                .refresh(param.refresh())
                .refreshTokenId(param.refreshTokenId())
                .expiration(param.expiration().toInstant())
                .build();
        refreshJpaRepository.save(refreshEntity);
        return refreshEntity;
    }

    @Override
    @Transactional
    public void revokeByEmail(DeleteRefreshByEmailParam param) {
        // 해당 이메일의 아직 무효화되지 않은 리프레시 행을 소프트 삭제(무효화)
        refreshJpaRepository.revokeByEmail(param.email(), Instant.now());
    }

    @Override
    public Refresh findByRefreshTokenIdAndRevokedFalse(String refreshTokenId) {
        // 해당 refreshTokenId의 아직 무효화되지 않은 리프레시 행을 조회
        return refreshJpaRepository.findByRefreshTokenIdAndRevokedFalse(refreshTokenId);
    }

    @Override
    @Transactional
    public long deleteExpiredBefore(Instant cutoff) {
        // 만료 시각이 cutoff보다 이전인 행을 물리 삭제
        return refreshJpaRepository.deleteByExpirationBefore(cutoff);
    }

}
