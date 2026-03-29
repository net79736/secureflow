package com.tdd.secureflow.domain.loginhistory.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 이력 ({@code USER_LOGIN_HIS} 도메인). 접속/종료 시각은 UTC 기준 {@link Instant}.
 */
@Entity
@Table(name = "user_login_his")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_his_no")
    private Long id;

    // 사용자 식별자
    @Column(name = "user_id", nullable = false, length = 60)
    private String userId;

    // 로그인 IP
    @Column(name = "login_ip", length = 40)
    private String loginIp;

    // 로그인 시각
    @Column(name = "login_dttm", nullable = false)
    private Instant loginDttm;

    // 세션 종료 시각
    @Column(name = "final_dttm")
    private Instant finalDttm;

    // 로그인 브라우저
    @Column(name = "login_brwsr", length = 20)
    private String loginBrowser;

    // 로그인 브라우저 버전
    @Column(name = "brwsr_ver", length = 20)
    private String browserVersion;

    // 로그인 상태 (성공: Y, 실패: N)
    @Column(name = "login_yn", nullable = false, length = 1)
    private String loginYn;

    @Builder
    public UserLoginHistory(
            String userId,
            String loginIp,
            Instant loginDttm,
            Instant finalDttm,
            String loginBrowser,
            String browserVersion,
            String loginYn
    ) {
        this.userId = userId;
        this.loginIp = loginIp;
        this.loginDttm = loginDttm;
        this.finalDttm = finalDttm;
        this.loginBrowser = loginBrowser;
        this.browserVersion = browserVersion;
        this.loginYn = loginYn;
    }

    /**
     * 세션 종료 시각을 기록합니다.
     * @param endedAt 세션 종료 시각
     */
    public void closeSession(Instant endedAt) {
        this.finalDttm = endedAt; // 세션 종료 (로그아웃 등) 시각
    }
}
