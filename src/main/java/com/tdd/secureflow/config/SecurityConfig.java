package com.tdd.secureflow.config;

import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tdd.secureflow.domain.common.util.UUIDKeyGenerator;
import com.tdd.secureflow.domain.loginhistory.service.LoginHistoryService;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.user.domain.model.UserRole;
import com.tdd.secureflow.domain.user.service.UserCommandService;
import com.tdd.secureflow.interfaces.WebConfig;
import com.tdd.secureflow.security.filter.JwtAuthenticationFilter;
import com.tdd.secureflow.security.filter.JwtAuthorizationFilter;
import com.tdd.secureflow.security.handler.AuthenticationEntryPointHandler;
import com.tdd.secureflow.security.handler.CustomAccessDeniedHandler;
import com.tdd.secureflow.security.handler.CustomLogoutSuccessHandler;
import com.tdd.secureflow.security.jwt.JwtProvider;
import com.tdd.secureflow.security.jwt.exception.JwtExceptionFilter;
import com.tdd.secureflow.security.oauth2.handler.CustomOauth2SuccessHandler;
import com.tdd.secureflow.security.oauth2.handler.OAuth2LoginFailureHandler;
import com.tdd.secureflow.security.oauth2.service.CustomOAuth2UserService;
import com.tdd.secureflow.security.service.CustomUserDetailsService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
//@EnableWebSecurity(debug = true)
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /* 권한 제외 대상 */
    private static final String[] PERMIT_ALL_URLS = new String[]{
            "/", "/index", "/login", "/signup"
    };
    /* User 접근 권한 */
    private static final String[] PERMIT_USER_URLS = new String[]{
            "/api/accounts/member"
    };
    /* Admin 접근 권한 */
    private static final String[] PERMIT_ADMIN_URLS = new String[]{
            "/api/accounts/admin"
    };

    @Value("${FRONT_URL:http://localhost:8082}")
    private String frontUrl;
    private final JwtProvider jwtProvider;
    private final WebConfig webConfig;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshRepository refreshRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOauth2SuccessHandler customOauth2SuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final UUIDKeyGenerator uuidKeyGenerator;
    private final LoginHistoryService loginHistoryService;
    private final UserCommandService userCommandService;

    @PostConstruct
    public void init() {
        log.debug("init security config");
        log.debug("frontUrl = {}", frontUrl);
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ADMIN > USER");
        return roleHierarchy;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        // HTTP 헤더 설정
        http.headers(headers -> headers
                .httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable) // HSTS 비활성화
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)        // FrameOptions 비활성화
        );


        /**
         * [ HTTP Basic 인증 비활성화 설정 ]
         * * 1. httpBasic 이란?
         * - 브라우저가 사용자에게 ID/PW를 요구할 때 띄우는 '기본 팝업창' 인증 방식입니다.
         * - 인증 정보(ID:PW)를 Base64로 인코딩하여 HTTP Header(Authorization)에 실어 보냅니다.
         * * 2. 왜 .disable() 하는가?
         * - 현대적인 웹(SPA, Mobile)에서는 브라우저 기본 팝업보다 직접 디자인한 '로그인 페이지'를 선호합니다.
         * - JWT(JSON Web Token) 등 토큰 기반 인증을 사용할 경우, 별도의 팝업 인증이 필요 없습니다.
         * - 보안상 Stateless한 API 서버를 구축할 때 세션/쿠키 방식의 기본 인증을 지양하기 위함입니다.
         * * 3. 보안 주의사항
         * - httpBasic은 데이터를 암호화하지 않고 인코딩만 하므로, HTTPS가 필수입니다.
         */
        // 기본 보안 설정 비활성화
        http.logout((auth) -> auth.disable()) // 로그아웃 비활성화
                .csrf((auth) -> auth.disable()) // csrf disable
                .formLogin((auth) -> auth.disable()) // From 로그인 방식 disable
                .httpBasic((auth) -> auth.disable()); // HTTP Basic 인증 방식 disable

        /**
         * JWT를 통한 인증/인가를 위해서 세션을 STATELESS 상태로 설정하는 것이 중요하다.
         *
         * STATELESS: 세션을 서버에서 유지하지 않습니다. 클라이언트가 요청할 때마다 필요한 인증 정보를 요청과 함께 보내야 합니다. 서버는 상태를 유지하지 않으며, 요청이 독립적으로 처리됩니다.
         * STATEFUL: 세션을 서버에서 관리합니다. 클라이언트가 로그인하면 세션이 생성되어 서버에 저장됩니다. 이후 요청은 이 세션을 통해 인증됩니다.
         *
         * # STATELESS 설정의 의미
         * SessionCreationPolicy.STATELESS를 설정하면, 서버가 클라이언트의 세션을 유지하지 않겠다는 뜻입니다. 즉, 서버는 각 요청을 독립적으로 처리하며, 클라이언트는 인증 정보를 매 요청마다 포함해야 합니다.
         * 이 방식은 주로 RESTful API에서 사용되며, 세션 기반 인증 대신 토큰 기반 인증(JWT 등)을 사용하는 경우에 적합합니다.
         */
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // OAuth2 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customOauth2SuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
        );

        // cors 설정
        http.cors((corsCustomizer) -> corsCustomizer.configurationSource(configurationSource()));

        // JWT 인증 및 토큰 검증 필터 추가
        http
            .addFilterBefore(jwtExceptionFilter, SecurityContextHolderFilter.class) // JWT 예외 필터를 가장 먼저 실행
            .addFilterBefore(new JwtAuthenticationFilter(authenticationManager, jwtProvider, refreshRepository, uuidKeyGenerator, loginHistoryService, userCommandService), UsernamePasswordAuthenticationFilter.class) // 로그인 필터 (아이디/비밀번호 검증)
            .addFilterBefore(new JwtAuthorizationFilter(jwtProvider, refreshRepository), JwtAuthenticationFilter.class); // JWT 토큰 인증 필터

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/auth/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler(new CustomLogoutSuccessHandler(jwtProvider, refreshRepository, loginHistoryService))
                .permitAll()
        );

        // 예외 처리 핸들러 설정
        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new AuthenticationEntryPointHandler())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
        );

        // 경로별 인가 작업
        http.authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers(PERMIT_ALL_URLS).permitAll()
                        .requestMatchers(PERMIT_USER_URLS).hasAnyAuthority(UserRole.USER.name())
                        .requestMatchers(PERMIT_ADMIN_URLS).hasAnyAuthority(UserRole.ADMIN.name())
                        .anyRequest().permitAll()                   // 나머지 요청은 모두 허용
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(customUserDetailsService);
        // 로그인 실패 이유를 구체적으로 구분하고 싶을 때 사용하는 설정
        provider.setHideUserNotFoundExceptions(false);  // 예외 숨김 해제 (별도 Exception 으로 처리하기)
        return new ProviderManager(provider);
    }

    public CorsConfigurationSource configurationSource() {
        System.out.println("configurationSource cors 설정이 SecurityFilterChain에 등록됨");
        CorsConfiguration configuration = new CorsConfiguration();
        
        // [허용할 HTTP 헤더] 설정
        // 클라이언트가 요청 시 보낼 수 있는 헤더를 지정합니다. "*"은 모든 헤더를 허용합니다.
        // 예: Content-Type, Authorization, Custom-Header 등
        configuration.addAllowedHeader("*");

        // [허용할 HTTP 메서드] 설정
        // GET, POST, PUT, DELETE, PATCH 등 브라우저가 보낼 수 있는 요청 방식을 제한합니다.
        // "*"은 모든 표준 메서드를 허용합니다.
        configuration.addAllowedMethod("*");

        configuration.addAllowedOriginPattern(frontUrl); // 프론트 앤드 IP만 허용 react
        configuration.addAllowedOriginPattern("http://localhost:3000"); // 프론트 앤드 IP만 허용 react

        /**
         * CORS는 클라이언트가 다른 도메인에 있는 리소스에 접근할 때 브라우저가 이를 안전하게 관리하는 메커니즘입니다.
         * 기본적으로, CORS 요청은 자격 증명을 포함하지 않습니다. 즉, 클라이언트가 보낸 요청에는 쿠키, HTTP 인증 헤더 등이 포함되지 않습니다.
         *
         * 대부분의 경우, 서버는 인증된 사용자만 특정 리소스에 접근할 수 있도록 설정되어 있습니다. 이 경우 쿠키(세션 ID 등)나 토큰(예: JWT)을 통해 사용자를 인증합니다.
         * 클라이언트가 인증된 요청을 보내기 위해서는 이러한 자격 증명이 요청에 포함되어야 합니다.
         *
         * 이 설정을 통해 브라우저는 클라이언트가 요청에 자격 증명을 포함할 수 있도록 허용합니다.
         * 이 설정이 없으면, 브라우저는 쿠키나 인증 헤더를 포함하지 않고 요청을 보냅니다.
         *
         * 즉, setAllowCredentials(true)가 설정되어 있어야 클라이언트가 서버에 요청을 보낼 때 쿠키를 포함할 수 있으며, 이로 인해 세션 유지나 사용자 인증을 할 수 있습니다.
         */
        configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청 허용
        
        // [응답 헤더 노출(Expose Header)] 설정
        // 브라우저는 기본적으로 보안을 위해 자바스크립트가 응답 헤더의 일부만 읽을 수 있게 제한합니다.
        // 아래 설정은 프론트엔드(React/Axios 등)에서 서버가 보낸 JWT 토큰 헤더를 직접 읽을 수 있게 허용하는 설정입니다.
        configuration.addExposedHeader(HEADER_AUTHORIZATION); // 보통 "Authorization"
        configuration.addExposedHeader(REFRESH_TOKEN_KEY);    // 커스텀 리프레시 토큰 헤더 (예: "Refresh-Token")

        /**
         * [ URL 기반 CORS ] 설정
         * 특정 URL 패턴에 대해 CORS 설정을 적용합니다.
         * "/**"는 모든 URL 경로에 대해 적용됩니다.
         */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

