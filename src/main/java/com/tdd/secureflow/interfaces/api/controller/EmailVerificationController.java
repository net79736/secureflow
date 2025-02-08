package com.tdd.secureflow.interfaces.api.controller;

import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.SendVerificationEmailRequest;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.SendVerificationEmailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface EmailVerificationController {

    @Operation(summary = "이메일 인증 코드 발송", description = "이메일 인증 코드를 발송합니다.")
    ResponseEntity<SendVerificationEmailResponse> sendVerification(
            @Schema(description = "이메일", example = "korea@naver.com") SendVerificationEmailRequest request,
            BindingResult bindingResult
    );

}
