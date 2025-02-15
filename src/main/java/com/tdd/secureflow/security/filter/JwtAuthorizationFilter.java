package com.tdd.secureflow.security.filter;

import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.domain.model.UserRole;
import com.tdd.secureflow.security.dto.CustomUserDetails;
import com.tdd.secureflow.security.jwt.JwtProvider;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.tdd.secureflow.domain.support.error.ErrorType.INVALID_ACCESS_TOKEN;
import static com.tdd.secureflow.domain.support.error.ErrorType.INVALID_TOKEN_TYPE;
import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        String accessToken = jwtProvider.getAccessToken(authorizationHeader);

        if (StringUtils.isNotEmpty(accessToken)) {
            if (!jwtProvider.validToken(accessToken)) {
                log.warn("인증되지 않은 토큰입니다");
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, INVALID_ACCESS_TOKEN.name());
                return;
            }

            Boolean expired = jwtProvider.isExpired(accessToken);
            if (expired) {
                log.warn("토큰이 만료되었습니다.");
//                sendErrorResponse(response, HttpStatus.GONE, ACCESS_TOKEN_EXPIRED.name());
//                return;
                throw new JwtException("ACCESS TOKEN IS EXPIRED");
            }

            String accessCategory = jwtProvider.getCategory(accessToken);
            if (!TOKEN_CATEGORY_ACCESS.equals(accessCategory)) {
                sendErrorResponse(response, HttpStatus.BAD_REQUEST, INVALID_TOKEN_TYPE.name());
                return;
            }

            String email = jwtProvider.getEmail(accessToken);
            String role = jwtProvider.getRole(accessToken);
            String status = jwtProvider.getStatus(accessToken);

            log.info("email : " + email);
            log.info("role : " + role);
            log.info("status : " + status);

            User user = User.builder()
                    .email(email)
                    .role(UserRole.valueOf(role))
                    .build();

            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("SecurityContext 에 인증 정보 저장 완료");
        }
        filterChain.doFilter(request, response);
    }


    /**
     * 공통 에러 응답 처리 메서드
     *
     * @param response   HttpServletResponse
     * @param httpStatus HTTP 상태 오브젝트
     * @param message    메시지
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"status\":\"%s\", \"message\":\"%s\"}", httpStatus.name(), message));
    }
}
