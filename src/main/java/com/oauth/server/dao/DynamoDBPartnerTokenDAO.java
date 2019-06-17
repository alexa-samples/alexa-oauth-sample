/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.oauth.server.dto.OAuthPartnerToken;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.ClientKeyGenerator;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.client.token.DefaultClientKeyGenerator;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * A DAO to access {@link OAuthPartnerToken} in DynamoDB.
 *
 * @author Lucun Cai
 */
public class DynamoDBPartnerTokenDAO implements ClientTokenServices {

    private DynamoDBMapper dynamoDBMapper;

    private ClientKeyGenerator keyGenerator;

    public DynamoDBPartnerTokenDAO(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
        this.keyGenerator = new DefaultClientKeyGenerator();
    }

    /**
     * Get the {@link OAuth2AccessToken} of a protected resource for the {@link Authentication} provided.
     *
     * @param resource partner protected resource.
     * @param authentication user authentication.
     * @return oauth access token.
     */
    @Override
    public OAuth2AccessToken getAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String authenticationId = keyGenerator.extractKey(resource, authentication);
        List<OAuthPartnerToken> accessTokens = getOAuthPartnerTokensByAuthenticationId(authenticationId);

        return accessTokens.stream().findAny().map(OAuthPartnerToken::getToken).orElse(null);
    }

    /**
     * Save the {@link OAuth2AccessToken} of a partner protected resource for the {@link Authentication} provided.
     *
     * @param resource partner protected resource.
     * @param authentication user authentication.
     * @param accessToken oauth access token.
     */
    @Override
    public void saveAccessToken(OAuth2ProtectedResourceDetails resource,
                                Authentication authentication,
                                OAuth2AccessToken accessToken) {

        String userName = authentication != null ? authentication.getName() : null;

        OAuthPartnerToken oauthPartnerToken = OAuthPartnerToken.builder()
            .tokenId(accessToken.getValue())
            .token(accessToken)
            .authenticationId(keyGenerator.extractKey(resource, authentication))
            .userName(userName)
            .clientId(resource.getClientId())
            .build();

        dynamoDBMapper.save(oauthPartnerToken);
    }

    /**
     * Remove the all the access token of the partner protected resource for the {@link Authentication} provided.
     *
     * @param resource partner protected resource.
     * @param authentication user authentication.
     */
    @Override
    public void removeAccessToken(OAuth2ProtectedResourceDetails resource, Authentication authentication) {
        String authenticationId = keyGenerator.extractKey(resource, authentication);
        List<OAuthPartnerToken> accessTokens = getOAuthPartnerTokensByAuthenticationId(authenticationId);

        dynamoDBMapper.batchDelete(accessTokens);
    }

    private List<OAuthPartnerToken> getOAuthPartnerTokensByAuthenticationId(String authenticationId) {
        DynamoDBQueryExpression query = new DynamoDBQueryExpression<OAuthPartnerToken>()
            .withIndexName("authenticationId-index")
            .withConsistentRead(Boolean.FALSE)
            .withHashKeyValues(OAuthPartnerToken.builder()
                .authenticationId(authenticationId)
                .build());
        return dynamoDBMapper.query(OAuthPartnerToken.class, query);
    }

}
