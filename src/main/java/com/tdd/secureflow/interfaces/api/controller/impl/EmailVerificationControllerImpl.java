package com.tdd.secureflow.interfaces.api.controller.impl;

import com.tdd.secureflow.domain.mail.service.EmailVerificationService;
import com.tdd.secureflow.interfaces.api.controller.EmailVerificationController;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.SendVerificationEmailRequest;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.SendVerificationEmailResponse;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.VerifyCodeRequest;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.VerifyCodeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verification")
public class EmailVerificationControllerImpl implements EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<SendVerificationEmailResponse> sendVerification(@Valid @RequestBody SendVerificationEmailRequest request, BindingResult bindingResult) {
        log.info("send-certification email: {}", request.email());
        emailVerificationService.send(request.email());
        return ResponseEntity.ok(new SendVerificationEmailResponse("SUCCESS", "이메일 인증 코드 전송 성공", null));
    }

    @PostMapping("/certify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest verifyCodeRequest, BindingResult bindingResult) {
        String code = verifyCodeRequest.code(); // 인증 코드
        String email = verifyCodeRequest.email(); // 이메일
        emailVerificationService.validateVerifyCode(email, code);

        log.info("certify email: {} success", email);
        return ResponseEntity.ok(new VerifyCodeResponse("SUCCESS", "인증 코드 확인", null));
    }
}
