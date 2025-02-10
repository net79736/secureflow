package com.tdd.secureflow.domain.refresh.doamin.repository;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshParam;
import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;

public interface RefreshRepository {
    Boolean existsRefresh(RefreshRepositoryParam.ExistsRefreshByEmailParam param);

    Refresh createRefresh(CreateRefreshParam param);

    void deleteRefresh(DeleteRefreshParam param);
}
