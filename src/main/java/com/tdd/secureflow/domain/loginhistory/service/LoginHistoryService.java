package com.tdd.secureflow.domain.loginhistory.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tdd.secureflow.domain.loginhistory.model.UserLoginHistory;
import com.tdd.secureflow.domain.loginhistory.repository.UserLoginHistoryRepository;
import com.tdd.secureflow.global.util.HttpClientInfoExtractor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private static final String Y = "Y";
    private static final String N = "N";

    private final UserLoginHistoryRepository userLoginHistoryRepository;

    /**
     * 로그인 성공 시 로그인 이력을 저장합니다.
     * @param userId
     * @param request
     */
    @Transactional
    public void recordSuccessfulLogin(String userId, HttpServletRequest request) {
        if (userId == null) {
            return;
        }
        Instant now = Instant.now();
        // 이전에 로그아웃 없이 남아 있던 미종료 성공 세션은 이번 로그인 시각으로 종료 처리
        int closed = userLoginHistoryRepository.closeOpenSuccessSessions(userId, now);
        if (closed > 0) {
            log.debug("재로그인으로 이전 미종료 세션 {}건 종료 처리: userId={}", closed, userId);
        }

        String ua = request != null ? request.getHeader("User-Agent") : null;
        UserLoginHistory row = UserLoginHistory.builder()
                .userId(userId) // 로그인 아이디
                .loginIp(HttpClientInfoExtractor.clientIp(request)) // 로그인 IP
                .loginDttm(now) // 로그인 시각
                .finalDttm(null) // 세션 종료 시각
                .loginBrowser(HttpClientInfoExtractor.browserSummary(ua)) // 로그인 브라우저
                .browserVersion(HttpClientInfoExtractor.browserVersionSummary(ua)) // 로그인 브라우저 버전
                .loginYn(Y) // 로그인 상태 (성공: Y, 실패: N)
                .build();
        userLoginHistoryRepository.save(row);
    }

    /**
     * 로그인 실패 시 로그인 이력을 저장합니다.
     * @param attemptedUserId
     * @param request
     */
    @Transactional
    public void recordFailedLogin(String attemptedUserId, HttpServletRequest request) {
        String uid = attemptedUserId != null ? attemptedUserId : "unknown";
        Instant at = Instant.now();
        String ua = request != null ? request.getHeader("User-Agent") : null;
        UserLoginHistory row = UserLoginHistory.builder()
                .userId(uid) // 로그인 시도 아이디
                .loginIp(HttpClientInfoExtractor.clientIp(request)) // 로그인 IP
                .loginDttm(at) // 로그인 시각
                .finalDttm(at) // 세션 종료 시각
                .loginBrowser(HttpClientInfoExtractor.browserSummary(ua)) // 로그인 브라우저
                .browserVersion(HttpClientInfoExtractor.browserVersionSummary(ua)) // 로그인 브라우저 버전
                .loginYn(N) // 로그인 상태 (성공: Y, 실패: N)
                .build();
        userLoginHistoryRepository.save(row);
    }

    /**
     * 로그아웃 시 가장 최근의 미종료 성공 세션에 종료 시각을 기록합니다.
     */
    @Transactional
    public void closeLatestOpenSession(String userId) {
        if (userId == null) {
            return;
        }
        userLoginHistoryRepository.findLatestOpenSession(userId).ifPresent(his -> {
            if (!Y.equals(his.getLoginYn())) {
                return;
            }
            his.closeSession(Instant.now());
            userLoginHistoryRepository.save(his);
        });
    }
}
