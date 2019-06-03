/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server;

import com.google.common.collect.ImmutableList;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.oauth.server.authentication.RoleEnum;
import com.oauth.server.dto.OAuthClientDetails;
import com.oauth.server.dto.OAuthPartner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SampleDataLoader {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void loadSampleData() {
        OAuthClientDetails testAlexaClient =
            OAuthClientDetails.builder()
                .clientId("test_alexa_client")
                .clientSecret(passwordEncoder.encode("test_client_secret"))
                .scopes("profile")
                .webServerRedirectUri("https://pitangui.amazon.com/api/skill/link/M3KVOEXUO4ALBL")
                .accessTokenValidity(3600)
                .refreshTokenValidity(0)
                .authorizedGrantTypes("implicit,authorization_code,refresh_token")
                .build();

        OAuthClientDetails adminClient =
            OAuthClientDetails.builder()
                .clientId("test_admin_client")
                .clientSecret(passwordEncoder.encode("test_client_secret"))
                .scopes("test_scope")
                .webServerRedirectUri("http://localhost:5000/redirect")
                .accessTokenValidity(3600)
                .refreshTokenValidity(0)
                .authorities(RoleEnum.ROLE_CLIENT_ADMIN.name())
                .authorizedGrantTypes("client_credentials,implicit,authorization_code,password,refresh_token")
                .build();

        OAuthPartner testAlexaPartner =
            OAuthPartner.builder()
                .partnerId("test_alexa_client")
                .clientId("amzn1.application-oa2-client.0897266ee6fb480ead86d615e2653558")
                .clientSecret("8241c286e8eb9c9741ce5b9e009c892f0bd4d603b21e51dc37efb5981245191a")
                .scopes("alexa::health:profile:write")
                .accessTokenUri("https://api.amazon.com/auth/o2/token")
                .userAuthorizationUri("https://www.amazon.com/ap/oa")
                .preEstablishedRedirectUri("")
                .build();

        dynamoDBMapper.batchSave(
            ImmutableList.of(testAlexaClient, adminClient, testAlexaPartner));
    }
}
