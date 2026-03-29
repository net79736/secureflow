package com.tdd.secureflow.domain.refresh.doamin.repository;

import java.time.Instant;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshByEmailAndRefreshAndExpirationParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.ExistsRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;

public interface RefreshRepository {
    Boolean existsByEmailAndRevokedFalse(ExistsRefreshByEmailParam param);

    Refresh createRefresh(CreateRefreshByEmailAndRefreshAndExpirationParam param);

    /**
     * 해당 이메일의 활성(refresh) 토큰을 무효화합니다. 행은 DB에 남고 {@code revoked}만 설정됩니다.
     */
    void revokeByEmail(DeleteRefreshByEmailParam param);

    Refresh findByRefreshTokenIdAndRevokedFalse(String refreshTokenId);

    /**
     * 만료된 리프레시 토큰 레코드를 삭제합니다. {@code cutoff}는 “이 시각 이전에 만료된 것” 기준입니다.
     *
     * @return 삭제된 행 수
     */
    long deleteExpiredBefore(Instant cutoff);
}
