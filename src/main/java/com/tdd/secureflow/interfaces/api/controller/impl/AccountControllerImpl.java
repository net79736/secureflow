package com.tdd.secureflow.interfaces.api.controller.impl;

import com.tdd.secureflow.domain.common.base.ResponseDto;
import com.tdd.secureflow.domain.user.service.UserQueryService;
import com.tdd.secureflow.interfaces.api.controller.AccountController;
import com.tdd.secureflow.interfaces.api.dto.AccountControllerDto.CheckDuplicateAccountRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tdd.secureflow.domain.common.base.ResponseStatus.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountControllerImpl implements AccountController {

    private final UserQueryService userQueryService;

    @Override
    @GetMapping("/check-duplicate")
    public ResponseEntity<ResponseDto> checkDuplicateAccount(@Valid CheckDuplicateAccountRequest request, BindingResult bindingResult) {
        log.info("Check duplicate account");
        userQueryService.existsByEmail(request.email());
        return ResponseEntity.ok(new ResponseDto(SUCCESS.getValue(), "계정 중복 체크 성공", null));
    }

    @GetMapping("/member")
    public ResponseEntity<String> memberPage() {
        log.info("Accessed member page");
        return ResponseEntity.ok("멤버 페이지 접근 성공!");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminPage() {
        log.info("Accessed admin page");
        return ResponseEntity.ok("관리자 페이지 접근 성공!");
    }
}
