package com.tdd.secureflow.infra.db.loginhistory;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tdd.secureflow.domain.loginhistory.model.UserLoginHistory;

public interface UserLoginHistoryJpaRepository extends JpaRepository<UserLoginHistory, Long> {

    Optional<UserLoginHistory> findTopByUserIdAndFinalDttmIsNullOrderByLoginDttmDesc(String userId);

    /**
     * 재로그인 등으로 이전 세션을 끊을 때, 미종료 성공 이력을 한 번에 종료 처리합니다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserLoginHistory h
            SET h.finalDttm = :closedAt
            WHERE h.userId = :userId
              AND h.finalDttm IS NULL
              AND h.loginYn = 'Y'
            """)
    int closeOpenSuccessSessions(@Param("userId") String userId, @Param("closedAt") Instant closedAt);
}
