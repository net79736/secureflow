package com.tdd.secureflow.domain.loginhistory.repository;

import java.time.Instant;
import java.util.Optional;

import com.tdd.secureflow.domain.loginhistory.model.UserLoginHistory;

public interface UserLoginHistoryRepository {

    UserLoginHistory save(UserLoginHistory entity);

    /**
     * 해당 사용자의 아직 종료되지 않은( {@code finalDttm == null} ) 가장 최근 이력.
     */
    Optional<UserLoginHistory> findLatestOpenSession(String userId);

    /**
     * 성공 로그인({@code loginYn=Y}) 중 {@code finalDttm}이 비어 있는 행을 모두 {@code closedAt}으로 종료합니다.
     *
     * @return 갱신된 행 수
     */
    int closeOpenSuccessSessions(String userId, Instant closedAt);
}
