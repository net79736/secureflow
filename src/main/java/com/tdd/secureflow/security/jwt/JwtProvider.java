package com.tdd.secureflow.security.jwt;

import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;

    /**
     * 토큰 발급
     *
     * @param duration Duration 만료 기간
     * @param username String
     * @param role     String
     * @return String
     */
    public String generateToken(String category, Duration duration, String username, String role, String refreshTokenId) {
        Date now = new Date();
        return makeToken(category, new Date(now.getTime() + duration.toMillis()), username, role, refreshTokenId);
    }

    /**
     * 토큰 생성
     *
     * @param category       토큰 종류 구분 (access | refresh)
     * @param expirationDate 만료 기간
     * @param username       이메일
     * @param role           권한
     * @return
     */
    private String makeToken(String category, Date expirationDate, String username, String role, String refreshTokenId) {
        Map<String, Object> extraClaims = buildExtraClaims(category, username, role, refreshTokenId);

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(expirationDate)
                .claims(extraClaims)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 사용자 정보를 기반으로 JWT에 포함할 추가 클레임을 생성합니다.
     *
     * @param userDetails 사용자 인증 정보 객체
     * @return JWT에 포함될 사용자 정의 클레임 맵
     */
    private Map<String, Object> buildExtraClaims(String category, String username, String role, String refreshTokenId) {
        HashMap<String, Object> extraClaims = new HashMap<>();

        extraClaims.put("category", category); // 카테고리
        extraClaims.put("email", username); // 계정
        extraClaims.put("role", role); // 사용자 권한
        extraClaims.put("refreshTokenId", refreshTokenId); // 리프레시 토큰 ID

        return extraClaims;
    }

    /**
     * 토큰으로부터 Authentication 객체를 가져옴
     *
     * @param token String
     * @return Authentication
     */
    public Authentication getAuthentication(final String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class)));
        return new UsernamePasswordAuthenticationToken(UUID.fromString(claims.get("id", String.class)), token,
                authorities);
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token String
     * @return boolean
     */
    public boolean validToken(final String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료된 토큰은 구조적으로 유효하다고 판단
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰으로부터 사용자 email를 추출
     *
     * @param token String
     * @return Email
     */
    public String getEmail(final String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * 토큰으로부터 사용자 권한(Authorities)을 추출
     *
     * @param token String
     * @return Set<SimpleGrantedAuthority>
     */
    public String getRole(final String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰으로부터 사용자 상태(status)을 추출
     *
     * @param token String
     * @return String
     */
    public String getStatus(final String token) {
        Claims claims = getClaims(token);
        return claims.get("status", String.class);
    }

    /**
     * 토큰으로부터 Claims를 가져옴
     *
     * @param token String
     * @return Claims
     */
    private Claims getClaims(String token) {
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
        return claimsJws.getPayload();
    }

    /**
     * 서명 키 생성
     *
     * @return SecretKey
     */
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretKey()));
    }

    /**
     * authorization header 에서 access token 을 추출합니다.
     *
     * @param authorizationHeader : String authorization header
     * @return String access token
     */
    public String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_SCHEME)) {
            // BEARER_SCHEME ("Bearer ") 제거 및 공백 제거
            return authorizationHeader.substring(BEARER_SCHEME.length()).trim();
        }
        return null; // Authorization 헤더가 없거나 잘못된 형식일 경우 null 반환
    }


    /**
     * 토큰으로부터 카테고리를 추출
     *
     * @param token String
     * @return UUID
     */
    public String getCategory(String token) {
        Claims claims = getClaims(token);
        return claims.get("category", String.class);
    }

    /**
     * 토큰이 만료되었는지 확인하는 메서드
     *
     * @param token JWT 토큰
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
     */
    public Boolean isExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            // 토큰 만료로 인한 예외 처리
            return true; // 만료된 것으로 판단
        }
    }

        /**
     * 토큰에서 refreshTokenId를 추출
     *
     * @param token String
     * @return refreshTokenId
     */
    public String getRefreshTokenId(final String token) {
        Claims claims = getClaims(token);
        return claims.get("refreshTokenId", String.class);
    }

    /**
     * 토큰의 만료시간을 추출하는 메서드
     *
     * @param token JWT 토큰
     * @return 토큰의 만료시간
     */
    public Date getExpiration(String token) {
        try {
            return getClaims(token).getExpiration();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 만료시간을 반환
            return e.getClaims().getExpiration();
        }
    }

    /**
     * 액세스 토큰의 만료 시간을 가져오는 메서드
     *
     * @return 액세스 토큰 만료 시간 (Duration)
     */
    public Duration getAccessTokenExpiration() {
        return Duration.ofMillis(jwtProperties.getAccessToken().getExpiration());
    }

    /**
     * 리프레시 토큰의 만료 시간을 가져오는 메서드
     *
     * @return 리프레시 토큰 만료 시간 (Duration)
     */
    public Duration getRefreshTokenExpiration() {
        return Duration.ofMillis(jwtProperties.getRefreshToken().getExpiration());
    }
}