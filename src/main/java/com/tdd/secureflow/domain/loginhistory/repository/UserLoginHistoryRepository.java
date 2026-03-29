package com.tdd.secureflow.domain.loginhistory.repository;

import java.util.Optional;

import com.tdd.secureflow.domain.loginhistory.model.UserLoginHistory;

public interface UserLoginHistoryRepository {

    UserLoginHistory save(UserLoginHistory entity);

    /**
     * 해당 사용자의 아직 종료되지 않은( {@code finalDttm == null} ) 가장 최근 이력.
     */
    Optional<UserLoginHistory> findLatestOpenSession(String userId);
}
