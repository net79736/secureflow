package com.tdd.secureflow.domain.user.domain.model;

import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.oauth2.OAuth2ServiceProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.tdd.secureflow.domain.support.error.ErrorType.Auth.UNSUPPORTED_OAUTH_PROVIDER;

@AllArgsConstructor
@Getter
public enum UserType {
    LOCAL("LOCAL"),
    NAVER(OAuth2ServiceProvider.NAVER),
    GOOGLE(OAuth2ServiceProvider.GOOGLE);

    private final String value;

    // OAuth2ServiceProvider 의 값과 일치하는 MemberType을 반환하는 메서드
    public static UserType fromOAuth2Provider(String provider) {
        for (UserType type : values()) {
            if (type.getValue().equals(provider)) {
                return type;
            }
        }
        throw new CoreException(UNSUPPORTED_OAUTH_PROVIDER, provider);
    }
}
