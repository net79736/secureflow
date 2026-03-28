package com.tdd.secureflow.interfaces.api.controller.impl;

import com.tdd.secureflow.domain.common.base.ResponseDto;
import com.tdd.secureflow.domain.mail.service.EmailVerificationService;
import com.tdd.secureflow.interfaces.api.controller.EmailVerificationController;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.SendVerificationEmailRequest;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.VerifyCodeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tdd.secureflow.domain.common.base.ResponseStatus.SUCCESS;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verification")
public class EmailVerificationControllerImpl implements EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<ResponseDto> sendVerification(@Valid @RequestBody SendVerificationEmailRequest request, BindingResult bindingResult) {
        log.info("send-certification email: {}", request.email());
        // [디버그] 브레이크포인트 ① — Tomcat HTTP 스레드 (예: http-nio-8082-exec-1)
        log.info("[mail-async-flow] ① controller BEFORE service | thread={} | email={}", Thread.currentThread().getName(), request.email());
        emailVerificationService.send(request.email());
        // [디버그] 브레이크포인트 ② — 아직 같은 HTTP 스레드. sendAsync 본문은 아직 끝나지 않았을 수 있음
        log.info("[mail-async-flow] ② controller AFTER service return | thread={} (①과 이름이 같아야 정상)", Thread.currentThread().getName());

        return ResponseEntity.ok(new ResponseDto(SUCCESS.getValue(), "이메일 인증 코드 전송 성공", null));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest verifyCodeRequest, BindingResult bindingResult) {
        String code = verifyCodeRequest.code(); // 인증 코드
        String email = verifyCodeRequest.email(); // 이메일
        emailVerificationService.validateVerifyCode(email, code);

        log.info("certify email: {} success", email);
        return ResponseEntity.ok(new ResponseDto(SUCCESS.getValue(), "인증 코드 확인", null));
    }
}
