package com.tdd.secureflow.oauth2.service;

import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.domain.model.UserType;
import com.tdd.secureflow.domain.user.dto.UserRepositoryParam;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import com.tdd.secureflow.global.util.PasswordUtil;
import com.tdd.secureflow.oauth2.exception.ExistingUserAuthenticationException;
import com.tdd.secureflow.oauth2.model.CustomOAuth2User;
import com.tdd.secureflow.oauth2.model.response.GoogleResponse;
import com.tdd.secureflow.oauth2.model.response.NaverResponse;
import com.tdd.secureflow.oauth2.model.response.OAuth2Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.tdd.secureflow.domain.support.error.ErrorType.Auth.UNSUPPORTED_OAUTH_PROVIDER;
import static com.tdd.secureflow.domain.user.domain.model.UserRole.USER;
import static com.tdd.secureflow.oauth2.OAuth2ServiceProvider.GOOGLE;
import static com.tdd.secureflow.oauth2.OAuth2ServiceProvider.NAVER;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("CustomOAuth2UserService > Oauth2User Request: {}", oAuth2User.toString());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = createOAuth2Response(registrationId, oAuth2User.getAttributes());

        // 지원하지 않는 PROVIDER
        if (oAuth2Response == null) {
            throw new CoreException(UNSUPPORTED_OAUTH_PROVIDER);
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String providerId = createProviderId(oAuth2Response);
        User byEmail = userRepository.findByEmailOrNull(oAuth2Response.getEmail());

        if (byEmail != null) {
            return handleExistingMember(byEmail, oAuth2Response);
        } else {
            // 신규 회원
            log.info("CustomOAuth2UserService create NewUser");
            log.info("providerId : {}", providerId);
            log.info("email : {}", oAuth2Response.getEmail());
            log.info("name : {}", oAuth2Response.getName());
            log.info("MemberRole.ROLE_USER : {}", USER);
            log.info("Provider() : {}", oAuth2Response.getProvider());
            log.info("registrationId : {}", registrationId);

            return createNewMember(oAuth2Response, providerId); // 우선 Oauth2 사용자 테이블에 저장;
        }
    }

    /**
     * 기존 존재하는 회원 처리
     */
    private OAuth2User handleExistingMember(User user, OAuth2Response oAuth2Response) {
        User fetcedUser = userRepository.findByEmailOrNull(
                oAuth2Response.getEmail()
        );

        if (fetcedUser != null && (fetcedUser.getType() != UserType.LOCAL)) {
            // 로그인한 소셜 정보가 이미 존재하는 경우 업데이트 처리
            log.debug("CustomOAuth2UserService isPresentUser");
            log.debug("providerId : {}", oAuth2Response.getProvider());
            log.debug("email : {}", oAuth2Response.getEmail());
            log.debug("name : {}", oAuth2Response.getName());
            log.debug("provider : {}", oAuth2Response.getProvider());
            log.debug("provider.name : {}", UserType.fromOAuth2Provider(oAuth2Response.getProvider()).name());

            return new CustomOAuth2User(fetcedUser.getEmail(), fetcedUser.getRole().name());
        } else {
            // 로컬 이메일 계정으로 존재하는 유저
            log.info("로컬 이메일 계정으로 존재하는 유저");
            throw new ExistingUserAuthenticationException(
                    "로컬 이메일 계정으로 이미 가입된 이메일입니다: " + maskEmail(oAuth2Response.getEmail())
            );
        }
    }

    private OAuth2User createNewMember(OAuth2Response oAuth2Response, String providerId) {
        log.info("Creating new member for providerId: {}", providerId);

        User newUser = User.builder()
                .email(oAuth2Response.getEmail())
                .password(PasswordUtil.generateRandomPassword())
                .role(USER)
                .type(UserType.fromOAuth2Provider(oAuth2Response.getProvider()))
                .build();

        userRepository.createUser(new UserRepositoryParam.CreateUserParam(
                newUser.getEmail(),
                newUser.getPassword(),
                newUser.getName(),
                newUser.getRole(),
                newUser.getType()
        ));
        return new CustomOAuth2User(oAuth2Response.getEmail(), USER.name());
    }

    /**
     * provider 값을 사용하여 providerId 를 생성한다.
     */
    private String createProviderId(OAuth2Response oAuth2Response) {
        return oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
    }

    /**
     * provider 를 기준으로 OAuth2Response 를 생성한다.
     */
    private OAuth2Response createOAuth2Response(String registrationId, Map<String, Object> attributes) {
        switch (registrationId) {
            case NAVER:
                return new NaverResponse(attributes);
            case GOOGLE:
                return new GoogleResponse(attributes);
            default:
                return null;
        }
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        // 이메일 분리 (아이디와 도메인 부분)
        int atIndex = email.indexOf("@");
        String localPart = email.substring(0, atIndex); // 아이디 부분
        String domainPart = email.substring(atIndex);  // 도메인 부분

        // 아이디의 앞 3글자는 유지, 나머지는 '*'로 마스킹
        if (localPart.length() <= 3) {
            return localPart + domainPart;
        }

        String visiblePart = localPart.substring(0, 3); // 앞 3글자
        String maskedPart = "*".repeat(localPart.length() - 3); // 나머지는 '*'
        return visiblePart + maskedPart + domainPart;
    }
}