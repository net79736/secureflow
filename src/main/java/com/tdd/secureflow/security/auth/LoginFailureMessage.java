package com.tdd.secureflow.security.auth;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 폼 로그인 실패 시 응답 메시지·이력 정책을 한곳에서 관리합니다.
 */
@Getter
@RequiredArgsConstructor
public enum LoginFailureMessage {

    USER_NOT_FOUND("존재하지 않는 계정입니다.", false),
    BAD_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.", true),
    DISABLED("비활성화된 계정입니다. 관리자에게 문의하세요.", true),
    LOCKED("계정이 잠겨 있습니다. 관리자에게 문의하세요.", true),
    CREDENTIALS_EXPIRED("비밀번호 유효 기간이 만료되었습니다. 비밀번호를 재설정하세요.", true),
    ACCOUNT_EXPIRED("계정 유효 기간이 만료되었습니다.", true),
    UNKNOWN("로그인에 실패했습니다. 다시 시도해주세요.", true);

    private final String defaultMessage;
    private final boolean recordFailureHistory; // true: 실패 이력 남김, false: 실패 이력 남기지 않음

    public static LoginFailureMessage from(AuthenticationException failed) {
        if (failed instanceof UsernameNotFoundException) {
            return USER_NOT_FOUND; // 존재하지 않는 아이디(UsernameNotFound)는 이력에 남기지 않음 — 시도 이메일 추적·정책상 제외
        }
        if (failed instanceof BadCredentialsException) {
            return BAD_CREDENTIALS; // 아이디 또는 비밀번호가 올바르지 않음
        }
        if (failed instanceof DisabledException) {
            return DISABLED; // 비활성화된 계정입니다. 관리자에게 문의하세요.
        }
        if (failed instanceof LockedException) {
            return LOCKED; // 계정이 잠겨 있습니다. 관리자에게 문의하세요.
        }
        if (failed instanceof CredentialsExpiredException) {
            return CREDENTIALS_EXPIRED; // 비밀번호 유효 기간이 만료되었습니다. 비밀번호를 재설정하세요.
        }
        if (failed instanceof AccountExpiredException) {
            return ACCOUNT_EXPIRED; // 계정 유효 기간이 만료되었습니다.
        }
        return UNKNOWN; // 로그인에 실패했습니다. 다시 시도해주세요.
    }
}
