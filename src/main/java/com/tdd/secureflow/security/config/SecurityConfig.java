package com.tdd.secureflow.security.config;

import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.user.domain.model.UserRole;
import com.tdd.secureflow.interfaces.WebConfig;
import com.tdd.secureflow.oauth2.handler.CustomOauth2SuccessHandler;
import com.tdd.secureflow.oauth2.handler.OAuth2LoginFailureHandler;
import com.tdd.secureflow.oauth2.service.CustomOAuth2UserService;
import com.tdd.secureflow.security.filter.JwtAuthenticationFilter;
import com.tdd.secureflow.security.filter.JwtAuthorizationFilter;
import com.tdd.secureflow.security.handler.AuthenticationEntryPointHandler;
import com.tdd.secureflow.security.handler.CustomAccessDeniedHandler;
import com.tdd.secureflow.security.handler.CustomLogoutSuccessHandler;
import com.tdd.secureflow.security.jwt.JwtProvider;
import com.tdd.secureflow.security.jwt.exception.JwtExceptionFilter;
import com.tdd.secureflow.security.service.CustomUserDetailsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;

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

    @PostConstruct
    public void init() {
        log.debug("init security config");
        log.debug("frontUrl = {}", frontUrl);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ADMIN > USER");
        return roleHierarchy;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // HTTP 헤더 설정
        http.headers(headers -> headers
                .httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable) // HSTS 비활성화
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)        // FrameOptions 비활성화
        );

        // 기본 보안 설정 비활성화
        http.logout((auth) -> auth.disable()) // 로그아웃 비활성화
                .csrf((auth) -> auth.disable()) // csrf disable
                .formLogin((auth) -> auth.disable()) // From 로그인 방식 disable
                .httpBasic((auth) -> auth.disable()); // HTTP Basic 인증 방식 disable

        // 세션 관리: Stateless
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
                .addFilterBefore(new JwtAuthenticationFilter(authenticationManager(), jwtProvider, refreshRepository), UsernamePasswordAuthenticationFilter.class) // 로그인 필터 (아이디/비밀번호 검증)
                .addFilterBefore(new JwtAuthorizationFilter(jwtProvider), JwtAuthenticationFilter.class); // JWT 토큰 인증 필터

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/auth/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler(new CustomLogoutSuccessHandler(jwtProvider, refreshRepository))
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
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(customUserDetailsService);
        // 로그인 실패 이유를 구체적으로 구분하고 싶을 때 사용하는 설정
        provider.setHideUserNotFoundExceptions(false);  // 예외 숨김 해제 (별도 Exception 으로 처리하기)
        return new ProviderManager(provider);
    }

    public CorsConfigurationSource configurationSource() {
        System.out.println("configurationSource cors 설정이 SecurityFilterChain에 등록됨");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedOriginPattern(frontUrl); // TODO_ 추후 변경 해야함 배포시
        configuration.addAllowedOriginPattern("http://localhost:3000"); // TODO_ 추후 변경 해야함 배포시
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader(HEADER_AUTHORIZATION);
        configuration.addExposedHeader(REFRESH_TOKEN_KEY);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

