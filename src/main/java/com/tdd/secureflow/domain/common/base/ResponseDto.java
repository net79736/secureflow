package com.tdd.secureflow.domain.common.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResponseDto<T> {
    private final Integer status; // SUCCESS 성공, FAIL 실패
    private final String msg;
    private final T data; // <T> 이거 해주어야 되네...
}