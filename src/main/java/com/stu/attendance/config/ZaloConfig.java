package com.stu.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ZaloConfig {

    @Value("${zalo.api.url:https://openapi.zalo.me/v2.0}")
    private String zaloApiUrl;

    @Value("${zalo.app.id}")
    private String zaloAppId;

    @Value("${zalo.app.secret}")
    private String zaloAppSecret;

    @Value("${zalo.redirect.uri:http://localhost:8080/api/zalo/auth/callback}")
    private String zaloRedirectUri;

    @Bean("zaloRestTemplate")
    public RestTemplate zaloRestTemplate() {
        return new RestTemplate();
    }

    // Method to get authorization URL for Zalo login
    public String getAuthorizationUrl() {
        return "https://oauth.zaloapp.com/v4/permission"
                + "?app_id=" + zaloAppId
                + "&redirect_uri=" + zaloRedirectUri
                + "&state=attendance";
    }

    // Method to get the API URL
    public String getApiUrl() {
        return zaloApiUrl;
    }

    // Method to get app credentials
    public String getAppId() {
        return zaloAppId;
    }

    public String getAppSecret() {
        return zaloAppSecret;
    }

    public String getRedirectUri() {
        return zaloRedirectUri;
    }
}