package com.tdd.secureflow.security.filter;

import static com.tdd.secureflow.domain.support.error.ErrorType.ACCESS_TOKEN_EXPIRED;
import static com.tdd.secureflow.domain.support.error.ErrorType.INVALID_ACCESS_TOKEN;
import static com.tdd.secureflow.domain.support.error.ErrorType.INVALID_TOKEN_TYPE;
import static com.tdd.secureflow.domain.support.error.ErrorType.SESSION_REVOKED_BY_NEW_LOGIN;
import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.domain.model.UserRole;
import com.tdd.secureflow.security.dto.CustomUserDetails;
import com.tdd.secureflow.security.jwt.JwtProvider;

import io.jsonwebtoken.JwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 모든 주소에서 동작함 (토큰 검증)
 *
 * successfulAuthentication 메서드에서는 로그인 시 JWT를 생성하지만, 클라이언트가 서버에 요청할 때 이 토큰이 유효한지 검증하는 과정이 필요합니다.
 * JWTFilter는 이 검증 작업을 수행합니다.
 *
 * 사용자가 로그인한 후, 이후의 요청에서 이 JWT를 사용하여 사용자 인증을 계속 유지해야 합니다.
 * JWTFilter가 각 요청을 가로채어 JWT의 유효성을 확인하고, 유효하다면 인증 정보를 SecurityContextHolder에 설정하는 역할을 합니다.
 *
 * 즉, 로그인 과정에서 JWT를 생성하는 것은 첫 번째 단계이고, 그 이후의 요청에서 JWT의 유효성을 검사하고 인증 정보를 설정하는 것은 또 다른 중요한 단계입니다.
 * 이 두 과정이 함께 작동하여 전체적인 인증 흐름이 완성되는 것입니다.
 *
 * 결론적으로, JWTFilter는 JWT의 유효성을 검증하고, 이를 통해 요청을 안전하게 처리하기 위한 필수적인 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final RefreshRepository refreshRepository;

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
                throw new JwtException(ACCESS_TOKEN_EXPIRED.name());
            }

            String accessCategory = jwtProvider.getCategory(accessToken);
            // 리프레시 토큰으로 요청하는 경우 예외 처리
            if (!TOKEN_CATEGORY_ACCESS.equals(accessCategory)) {
                sendErrorResponse(response, HttpStatus.BAD_REQUEST, INVALID_TOKEN_TYPE.name());
                return;
            }

            String email = jwtProvider.getEmail(accessToken);
            String role = jwtProvider.getRole(accessToken);
            String status = jwtProvider.getStatus(accessToken);
            String refreshTokenId = jwtProvider.getRefreshTokenId(accessToken);

            log.info("email : " + email);
            log.info("role : " + role);
            log.info("status : " + status);

            // 현재 토큰이 유효한지 확인
            // TODO: 중복 로그인 로직 disabled 처리함 author jongwook
            try {
                // 리프레시 토큰 아이디로 리프레시 토큰 조회
                Refresh refresh = refreshRepository.findByRefreshTokenId(refreshTokenId);
                // 값이 없으면 다른 기기에서 로그인하여 토큰이 만료된 것으로 처리
                if (refresh == null || refresh.getRefreshTokenId() == null) {
                    log.warn("유효하지 않은 토큰 - refreshTokenId: {}", refreshTokenId);
                    throw new JwtException(SESSION_REVOKED_BY_NEW_LOGIN.name());
                }
                log.debug("토큰 유효성 검증 성공 - refreshToken: {}, refreshTokenId: {}", refresh.getRefresh(), refresh.getRefreshTokenId());
            } catch (CoreException e) {
                // 토큰이 유효하지 않으면 다른 기기에서 로그인한 것으로 간주
                throw new JwtException(SESSION_REVOKED_BY_NEW_LOGIN.name());
            }

            User user = User.builder()
                    .email(email)
                    .role(UserRole.valueOf(role))
                    .build();

            // CustomUserDetails 객체 생성
            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            // 스프링 시큐리티 인증 토큰 생성
            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

            // 세션에 사용자 등록 (인증 정보 저장)
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
