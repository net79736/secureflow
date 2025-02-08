package com.tdd.secureflow.domain.support.error;

import org.springframework.boot.logging.LogLevel;

public interface IErrorType {

    ErrorCode getCode();

    String getMessage();

    LogLevel getLogLevel();
}
