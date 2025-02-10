package com.tdd.secureflow.domain.support.error;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.logging.LogLevel;

@AllArgsConstructor
@Getter
public enum ErrorType implements IErrorType {
    INVALID_PARAMETER_REQUEST(ErrorCode.BAD_REQUEST, "유효하지 않은 파라미터입니다.", LogLevel.WARN),
    INVALID_REQUEST(ErrorCode.BAD_REQUEST, "유효하지 않은 요청입니다.", LogLevel.WARN),
    UNAUTHORIZED_ACCESS(ErrorCode.UNAUTHORIZED, "인증되지 않은 접근입니다.", LogLevel.ERROR),
    FORBIDDEN_ACCESS(ErrorCode.FORBIDDEN, "권한이 없는 접근입니다.", LogLevel.ERROR),
    ACCESS_TOKEN_EXPIRED(ErrorCode.UNAUTHORIZED, "어세스 토큰이 만료되었습니다.", LogLevel.WARN),
    INVALID_ACCESS_TOKEN(ErrorCode.BAD_REQUEST, "유효하지 않은 어세스 토큰입니다.", LogLevel.WARN),
    REFRESH_TOKEN_EXPIRED(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다.", LogLevel.WARN),
    REFRESH_TOKEN_NOT_FOUND(ErrorCode.BAD_REQUEST, "리프레시 토큰이 존재하지 않습니다.", LogLevel.WARN),
    INVALID_REFRESH_TOKEN(ErrorCode.BAD_REQUEST, "유효하지 않은 리프레시 토큰입니다.", LogLevel.WARN),
    INVALID_TOKEN_TYPE(ErrorCode.BAD_REQUEST, "잘못된 토큰 유형 입니다.", LogLevel.ERROR),
    INTERNAL_SERVER_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", LogLevel.ERROR);

    private final ErrorCode code;
    private final String message;
    private final LogLevel logLevel;

    @AllArgsConstructor
    public enum User implements IErrorType {
        USER_NOT_FOUND(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.", LogLevel.WARN),
        INVALID_CREDENTIALS(ErrorCode.UNAUTHORIZED, "잘못된 자격 증명입니다.", LogLevel.WARN),
        ACCOUNT_LOCKED(ErrorCode.FORBIDDEN, "계정이 잠겨 있습니다.", LogLevel.WARN),
        ACCOUNT_ALREADY_EXISTS(ErrorCode.FORBIDDEN, "이미 존재하는 계정입니다.", LogLevel.WARN),
        ACCOUNT_DISABLED(ErrorCode.FORBIDDEN, "계정이 비활성화되었습니다.", LogLevel.WARN),
        USER_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "사용자 ID는 null일 수 없습니다.", LogLevel.WARN),
        PASSWORD_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "비밀번호는 null일 수 없습니다.", LogLevel.WARN),
        CONFIRM_PASSWORD_NOT_MATCHING(ErrorCode.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다.", LogLevel.WARN);

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    @AllArgsConstructor
    public enum Auth implements IErrorType {
        UNSUPPORTED_OAUTH_PROVIDER(ErrorCode.BAD_REQUEST, "지원하지 않는 OAuth 제공자 입니다.", LogLevel.ERROR),
        AUTHENTICATION_FAILED(ErrorCode.UNAUTHORIZED, "인증에 실패했습니다.", LogLevel.ERROR),
        OAUTH_PROVIDER_ERROR(ErrorCode.BAD_REQUEST, "OAuth 제공자 오류가 발생했습니다.", LogLevel.WARN),
        TOKEN_GENERATION_FAILED(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 생성에 실패했습니다.", LogLevel.ERROR),
        TOKEN_VALIDATION_FAILED(ErrorCode.UNAUTHORIZED, "토큰 검증에 실패했습니다.", LogLevel.WARN);

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    // Email 관련 에러
    @AllArgsConstructor
    public enum Email implements IErrorType {
        EMAIL_SEND_FAILED(ErrorCode.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다.", LogLevel.ERROR),
        INVALID_EMAIL_FORMAT(ErrorCode.BAD_REQUEST, "올바르지 않은 이메일 형식입니다.", LogLevel.WARN),
        EMAIL_CODE_EXPIRED(ErrorCode.UNAUTHORIZED, "이메일 인증 코드가 만료되었습니다.", LogLevel.WARN),
        EMAIL_CODE_MISMATCH(ErrorCode.UNAUTHORIZED, "이메일 인증 코드가 일치하지 않습니다.", LogLevel.WARN),
        EMAIL_CODE_NOT_FOUND(ErrorCode.UNAUTHORIZED, "이메일 인증 코드가 존재하지 않습니다.", LogLevel.WARN),
        EMAIL_NOT_FOUND(ErrorCode.NOT_FOUND, "등록된 이메일을 찾을 수 없습니다.", LogLevel.WARN);

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }
}
