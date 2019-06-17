/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.oauth.server.dto.OAuthCode;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;

/**
 * A DAO to access {@link OAuth2Authentication} in DynamoDB.
 *
 * @author Lucun Cai
 */
public class DynamoDBAuthorizationCodeDAO extends RandomValueAuthorizationCodeServices {

    private DynamoDBMapper dynamoDBMapper;

    public DynamoDBAuthorizationCodeDAO(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Store the authorization code for a authenticated user.
     *
     * @param code authorization code.
     * @param authentication authentication for the user.
     */
    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        OAuthCode oAuthCode = new OAuthCode(code, authentication);
        dynamoDBMapper.save(oAuthCode);
    }

    /**
     * Remove/Invalidate the authorization code.
     *
     * @param code authorization code.
     * @return user authentication.
     */
    @Override
    public OAuth2Authentication remove(String code) {
        OAuthCode oAuthCode = dynamoDBMapper.load(new OAuthCode(code));

        if (oAuthCode == null) {
            return null;
        }

        dynamoDBMapper.delete(oAuthCode);

        return oAuthCode.getAuthentication();
    }
}
