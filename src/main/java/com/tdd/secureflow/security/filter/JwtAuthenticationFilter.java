package com.tdd.secureflow.security.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdd.secureflow.domain.loginhistory.service.LoginHistoryService;
import com.tdd.secureflow.domain.user.dto.UserCommand.RecordLoginFailureCommand;
import com.tdd.secureflow.domain.user.service.UserCommandService;
import com.tdd.secureflow.security.auth.LoginFailureMessage;
import com.tdd.secureflow.security.dto.CustomUserDetails;
import com.tdd.secureflow.security.login.LoginSessionIssuer;
import com.tdd.secureflow.security.login.LoginSessionIssuer.IssuedLoginSessionTokens;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    /** 로그인 실패 시 {@link #unsuccessfulAuthentication} 에서 조회 (시도한 아이디). */
    static final String ATTR_ATTEMPTED_USERNAME = JwtAuthenticationFilter.class.getName() + ".attemptedUsername";
    private final AuthenticationManager authenticationManager;
    private final LoginSessionIssuer loginSessionIssuer;
    private final LoginHistoryService loginHistoryService;
    private final UserCommandService userCommandService;

    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            LoginSessionIssuer loginSessionIssuer,
            LoginHistoryService loginHistoryService,
            UserCommandService userCommandService
    ) {
        setFilterProcessesUrl("/auth/login");
        this.authenticationManager = authenticationManager;
        this.loginSessionIssuer = loginSessionIssuer;
        this.loginHistoryService = loginHistoryService;
        this.userCommandService = userCommandService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // JSON 요청 본문에서 username과 password 추출
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);

            String username = credentials.get("username");
            String password = credentials.get("password");

            log.info("로그인 요청 - username: {}, password: {}", username, password);

            // [로그인 실패 시 시도한 아이디 저장]을 위한 속성 설정
            request.setAttribute(ATTR_ATTEMPTED_USERNAME, username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            log.error("로그인 요청 JSON 파싱 오류", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        try {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String username = customUserDetails.getUsername();
            log.info("successfulAuthentication > username : {}", username);

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();

            // 권한 획득
            String role = auth.getAuthority();

            IssuedLoginSessionTokens issued = loginSessionIssuer.issue(request, response, username, role);
            customUserDetails.getUser().setRefreshTokenId(issued.refreshTokenId()); // TODO: 리팩토링 고려 필요 author jongwook

            log.debug("print accessToken: {}", issued.accessToken());
            log.debug("print role: {}", role);
            response.setStatus(HttpStatus.OK.value());

            log.info("자체 서비스 로그인에 성공하였습니다.");
        } catch (InternalAuthenticationServiceException e) {
            log.error("successfulAuthentication 메서드 에러 발생 : {}", e.getMessage());
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {

        LoginFailureMessage kind = LoginFailureMessage.from(failed);
        String errorMessage = kind.getDefaultMessage(); // 로그인 실패 메시지

        // 로그인 실패 시 시도한 아이디
        String attemptedUserId = getAttemptedUserId(request);

        if (kind.isRecordFailureCnt()) {
            // 로그인 실패 시 실패 횟수 증가
            userCommandService.recordLoginFailure(new RecordLoginFailureCommand(attemptedUserId));
        }

        // 실패 이력 남기기 여부에 따라 로그인 실패 이력 저장. 존재하지 않는 아이디(UsernameNotFound)는 이력에 남기지 않음
        if (kind.isRecordFailureHistory()) {
            recordFailedLoginAttempt(request, attemptedUserId); // 로그인 실패 이력 저장
        }

        // 로그로 실패 메시지 출력
        log.warn("Authentication failed: {}", errorMessage);

        // 401 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // JSON 형식으로 에러 메시지 응답
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of("error", errorMessage)));
    }

    /**
     * 로그인 실패 시 시도한 아이디로 로그인 실패 이력을 남깁니다.
     * @param request 요청
     * @param attemptedUserId 로그인 실패 시 시도한 아이디
     */
    private void recordFailedLoginAttempt(HttpServletRequest request, String attemptedUserId) {
        loginHistoryService.recordFailedLogin(attemptedUserId, request); // 로그인 실패 이력 저장
    }

    /**
     * 로그인 실패 시 시도한 아이디 조회
     * @param request
     * @return 로그인 실패 시 시도한 아이디
     */
    private String getAttemptedUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(ATTR_ATTEMPTED_USERNAME);
        return attr != null ? attr.toString() : null;
    }
}
