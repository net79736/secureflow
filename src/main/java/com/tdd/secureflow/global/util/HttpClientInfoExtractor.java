package com.tdd.secureflow.global.util;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/**
 * 로그인 이력용 클라이언트 IP / 브라우저 요약 (User-Agent 파싱은 최소 수준).
 */
@UtilityClass
public class HttpClientInfoExtractor {

    private static final int BROWSER_MAX = 20;
    private static final int VERSION_MAX = 20;

    /**
     * 클라이언트 IP 조회
     * @param request
     * @return 클라이언트 IP (X-Forwarded-For, X-Real-IP, RemoteAddr 순으로 조회)
     */
    public static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xff)) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp.trim();
        }
        return StringUtils.defaultIfBlank(request.getRemoteAddr(), "");
    }

    /**
     * 브라우저 요약
     * @param userAgent
     * @return 브라우저 요약
     */
    public static String browserSummary(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return "Unknown";
        }
        String ua = userAgent;
        if (ua.contains("Edg/")) {
            return truncate("Edge", BROWSER_MAX);
        }
        if (ua.contains("Chrome/")) {
            return truncate("Chrome", BROWSER_MAX);
        }
        if (ua.contains("Firefox/")) {
            return truncate("Firefox", BROWSER_MAX);
        }
        if (ua.contains("Safari/") && !ua.contains("Chrome")) {
            return truncate("Safari", BROWSER_MAX);
        }
        return truncate(ua, BROWSER_MAX);
    }

    /**
     * 브라우저 버전 요약
     * @param userAgent
     * @return 브라우저 버전 요약
     */
    public static String browserVersionSummary(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return "";
        }
        // 간단히 첫 번째 버전 패턴
        int slash = userAgent.indexOf('/');
        if (slash > 0 && slash < userAgent.length() - 1) {
            int end = userAgent.indexOf(' ', slash);
            if (end < 0) {
                end = userAgent.length();
            }
            String ver = userAgent.substring(slash + 1, Math.min(end, slash + 15));
            return truncate(ver, VERSION_MAX);
        }
        return truncate(userAgent, VERSION_MAX);
    }

    /**
     * 문자열 자르기
     * @param s 문자열
     * @param max 최대 길이
     * @return 자른 문자열
     */
    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
