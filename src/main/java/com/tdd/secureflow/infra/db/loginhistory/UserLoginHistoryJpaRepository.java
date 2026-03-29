package com.tdd.secureflow.infra.db.loginhistory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tdd.secureflow.domain.loginhistory.model.UserLoginHistory;

public interface UserLoginHistoryJpaRepository extends JpaRepository<UserLoginHistory, Long> {

    // 해당 유저의 가장 최근 로그인 이력을 조회
    Optional<UserLoginHistory> findTopByUserIdAndFinalDttmIsNullOrderByLoginDttmDesc(String userId);
}
