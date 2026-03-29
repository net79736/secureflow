package com.tdd.secureflow.security.oauth2.attribute;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.tdd.secureflow.security.oauth2.OAuth2ServiceProvider;
import com.tdd.secureflow.security.oauth2.model.response.GoogleResponse;
import com.tdd.secureflow.security.oauth2.model.response.OAuth2Response;

@Component
public class GoogleOAuth2AttributeMapper implements OAuth2AttributeMapper {
    @Override
    public String getRegistrationId() {
        return OAuth2ServiceProvider.GOOGLE;
    }

    @Override
    public OAuth2Response map(Map<String, Object> attributes) {
        return new GoogleResponse(attributes);
    }
}
