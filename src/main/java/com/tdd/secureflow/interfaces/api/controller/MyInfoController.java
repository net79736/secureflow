package com.tdd.secureflow.interfaces.api.controller;

import com.tdd.secureflow.domain.common.base.ResponseDto;
import com.tdd.secureflow.interfaces.api.dto.MyInfoControllerDto.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface MyInfoController {

    @Operation(summary = "로컬 회원가입", description = "로컬 회원으로 가입 합니다.")
    ResponseEntity<ResponseDto> signUp(
            @Schema(description = "회원 가입 사용자 객체", example = "java.lang.Object") SignUpRequest request,
            BindingResult bindingResult
    );

}
