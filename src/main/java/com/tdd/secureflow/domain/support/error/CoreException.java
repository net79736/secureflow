package com.tdd.secureflow.domain.support.error;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class CoreException extends RuntimeException {

    private final IErrorType errorType;
    private final Object data;

    public CoreException(IErrorType errorType, Object data) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.data = data;
    }

    public CoreException(IErrorType errorType) {
        this(errorType, null);
    }

    /**
     * 예외 데이터를 JSON 문자열로 변환하는 메서드 (key, value 인자를 받아 data에 추가)
     */
    public static Map<String, Object> createErrorJson(String key, Object value) {
        Map<String, Object> response = new HashMap<>();
        response.put(key, value);
        try {
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
