package com.tdd.secureflow.domain.refresh.doamin.repository;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshByEmailAndRefreshAndExpirationParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.ExistsRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;

public interface RefreshRepository {
    Boolean existsRefresh(ExistsRefreshByEmailParam param);

    Refresh createRefresh(CreateRefreshByEmailAndRefreshAndExpirationParam param);

    void deleteRefresh(DeleteRefreshByEmailParam param);
}
