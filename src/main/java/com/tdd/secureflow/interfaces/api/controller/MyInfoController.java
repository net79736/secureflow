package com.tdd.secureflow.interfaces.api.controller;

import com.tdd.secureflow.domain.common.base.ResponseDto;
import com.tdd.secureflow.interfaces.api.dto.MyInfoControllerDto.SignUpRequest;
import com.tdd.secureflow.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface MyInfoController {

    @Operation(summary = "로컬 회원가입", description = "로컬 회원으로 가입 합니다.")
    ResponseEntity<ResponseDto> signUp(
            @Schema(description = "회원 가입 사용자 객체", example = "java.lang.Object") SignUpRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    );

    @Operation(summary = "로그인 회원 정보 조회", description = "로그인 회원의 정보를 조회 합니다.")
    ResponseEntity<ResponseDto> getMyInfo(CustomUserDetails userDetails);
}
