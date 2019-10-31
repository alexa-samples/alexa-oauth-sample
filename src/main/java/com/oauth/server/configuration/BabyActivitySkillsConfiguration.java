package com.oauth.server.configuration;

import com.oauth.server.authentication.PartnerTokenManager;
import com.oauth.server.babyactivityskills.BabyActivityProfilePublisher;
import com.oauth.server.babyactivityskills.HttpClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.concurrent.Executors;

@Configuration
public class BabyActivitySkillsConfiguration {

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private PartnerTokenManager partnerTokenManager;


    @Bean
    public BabyActivityProfilePublisher babyActivityProfilePublisher() {
        return new BabyActivityProfilePublisher(tokenStore, partnerTokenManager,
                new HttpClientFactory(), Executors.newSingleThreadExecutor());
    }
}
