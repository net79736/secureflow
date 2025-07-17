package com.tdd.secureflow.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientTypeLoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userAgent = request.getHeader("User-Agent");
        String clientType = "UNKNOWN";

        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("mozilla") || ua.contains("chrome") || ua.contains("safari") || ua.contains("firefox") || ua.contains("edge")) {
                clientType = "WEB";
            } else if (ua.contains("android") || ua.contains("iphone") || ua.contains("ipad") || ua.contains("mobile")) {
                clientType = "APP";
            }
        }

        // 커스텀 헤더 우선 적용 (앱에서 X-Client-Type: app/web 보내는 경우)
        String customType = request.getHeader("X-Client-Type");
        if (customType != null) {
            clientType = customType.equalsIgnoreCase("app") ? "APP" : "WEB";
        }

        log.info("[ClientTypeLoggingInterceptor] 요청 Client Type: {} | User-Agent: {}", clientType, userAgent);
        return true;
    }
} 