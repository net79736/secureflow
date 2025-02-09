package com.tdd.secureflow.interfaces.api.controller;

import com.tdd.secureflow.domain.common.base.ResponseDto;
import com.tdd.secureflow.interfaces.api.dto.AccountControllerDto.CheckDuplicateAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface AccountController {

    @Operation(summary = "계정 중복 체크", description = "계정을 중복 체크합니다.")
    ResponseEntity<ResponseDto> checkDuplicateAccount(
            @Schema(description = "이메일", example = "korea@naver.com") CheckDuplicateAccountRequest request,
            BindingResult bindingResult
    );

}
