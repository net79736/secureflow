package com.tdd.secureflow.security.oauth2.attribute;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.tdd.secureflow.security.oauth2.OAuth2ServiceProvider;
import com.tdd.secureflow.security.oauth2.model.response.NaverResponse;
import com.tdd.secureflow.security.oauth2.model.response.OAuth2Response;

@Component
public class NaverOAuth2AttributeMapper implements OAuth2AttributeMapper {
    @Override
    public String getRegistrationId() {
        return OAuth2ServiceProvider.NAVER;
    }

    @Override
    public OAuth2Response map(Map<String, Object> attributes) {
        return new NaverResponse(attributes);
    }
}
