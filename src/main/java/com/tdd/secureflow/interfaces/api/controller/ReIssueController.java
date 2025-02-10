package com.tdd.secureflow.interfaces.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface ReIssueController {

    @Operation(summary = "토큰 재발급", description = "토큰을 재발급 합니다.")
    ResponseEntity<?> reissue(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    );

}
