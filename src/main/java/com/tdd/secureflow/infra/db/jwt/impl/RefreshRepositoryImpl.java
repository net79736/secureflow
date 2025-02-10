package com.tdd.secureflow.infra.db.jwt.impl;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshByEmailAndRefreshAndExpirationParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.ExistsRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.infra.db.jwt.RefreshJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RefreshRepositoryImpl implements RefreshRepository {

    private final RefreshJpaRepository refreshJpaRepository;

    @Override
    public Boolean existsRefresh(ExistsRefreshByEmailParam param) {
        return refreshJpaRepository.existsByEmail(param.email());
    }

    @Override
    @Transactional
    public Refresh createRefresh(CreateRefreshByEmailAndRefreshAndExpirationParam param) {
        Refresh refreshEntity = Refresh.builder()
                .email(param.email())
                .refresh(param.refresh())
                .expiration(param.expiration().toInstant())
                .build();
        refreshJpaRepository.save(refreshEntity);
        return null;
    }

    @Override
    @Transactional
    public void deleteRefresh(DeleteRefreshByEmailParam param) {
        refreshJpaRepository.deleteByEmail(param.email());
    }


}
