package com.tdd.secureflow.infra.db.loginhistory.impl;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tdd.secureflow.domain.loginhistory.model.UserLoginHistory;
import com.tdd.secureflow.domain.loginhistory.repository.UserLoginHistoryRepository;
import com.tdd.secureflow.infra.db.loginhistory.UserLoginHistoryJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserLoginHistoryRepositoryImpl implements UserLoginHistoryRepository {

    private final UserLoginHistoryJpaRepository userLoginHistoryJpaRepository;

    @Override
    public UserLoginHistory save(UserLoginHistory entity) {
        return userLoginHistoryJpaRepository.save(entity);
    }

    @Override
    public Optional<UserLoginHistory> findLatestOpenSession(String userId) {
        // 해당 유저의 가장 최근 로그인 이력을 조회
        return userLoginHistoryJpaRepository.findTopByUserIdAndFinalDttmIsNullOrderByLoginDttmDesc(userId);
    }

    @Override
    @Transactional
    public int closeOpenSuccessSessions(String userId, Instant closedAt) {
        // 재로그인 등으로 이전 세션을 끊을 때, 미종료 성공 이력을 한 번에 종료 처리
        return userLoginHistoryJpaRepository.closeOpenSuccessSessions(userId, closedAt);
    }
}
