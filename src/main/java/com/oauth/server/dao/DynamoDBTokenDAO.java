/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.oauth.server.dto.OAuthAccessToken;
import com.oauth.server.dto.OAuthRefreshToken;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

/**
 * A DAO to access {@link OAuth2AccessToken} in DynamoDB. This is an implementation of token services that stores tokens in
 * DynamoDB. This was primarily based off of the functionality of the {@link JdbcTokenStore}.
 *
 * @author Lucun Cai
 */
public class DynamoDBTokenDAO implements TokenStore {

    private final AuthenticationKeyGenerator authenticationKeyGenerator;

    private final DynamoDBMapper dynamoDBMapper;

    public DynamoDBTokenDAO(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
        this.authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
    }

    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        String authenticationId = authenticationKeyGenerator.extractKey(authentication);

        DynamoDBQueryExpression query = new DynamoDBQueryExpression<OAuthAccessToken>()
            .withIndexName("authenticationId-index")
            .withConsistentRead(Boolean.FALSE)
            .withHashKeyValues(OAuthAccessToken.builder()
                .authenticationId(authenticationId)
                .build());

        List<OAuthAccessToken> accessTokens = dynamoDBMapper.query(OAuthAccessToken.class, query);

        return accessTokens.stream().findAny().map(OAuthAccessToken::getToken).orElse(null);


    }

    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        String refreshToken = null;
        if (token.getRefreshToken() != null) {
            refreshToken = token.getRefreshToken().getValue();
        }

        OAuthAccessToken accessToken = OAuthAccessToken.builder()
            .tokenId(extractTokenKey(token.getValue()))
            .token(token)
            .authenticationId(authenticationKeyGenerator.extractKey(authentication))
            .authentication(authentication)
            .clientId(authentication.getOAuth2Request().getClientId())
            .refreshToken(extractTokenKey(refreshToken))
            .userName(StringUtils.isNotBlank(authentication.getName()) ? authentication.getName() : "#")
            .build();

        dynamoDBMapper.save(accessToken);
    }

    public OAuth2AccessToken readAccessToken(String tokenValue) {
        String tokenId = extractTokenKey(tokenValue);

        return Optional.ofNullable(dynamoDBMapper.load(OAuthAccessToken.class, tokenId))
            .map(OAuthAccessToken::getToken)
            .orElse(null);
    }

    public void removeAccessToken(OAuth2AccessToken token) {
        removeAccessToken(token.getValue());
    }

    public void removeAccessToken(String tokenValue) {
        String tokenId = extractTokenKey(tokenValue);
        OAuthAccessToken itemToDelete = OAuthAccessToken.builder().tokenId(tokenId).build();

        dynamoDBMapper.delete(itemToDelete);
    }

    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    public OAuth2Authentication readAuthentication(String token) {
        String tokenId = extractTokenKey(token);
        return Optional.ofNullable(dynamoDBMapper.load(OAuthAccessToken.class, tokenId))
            .map(OAuthAccessToken::getAuthentication)
            .orElse(null);
    }

    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {

        OAuthRefreshToken itemToSave = OAuthRefreshToken.builder()
            .tokenId(extractTokenKey(refreshToken.getValue()))
            .token(refreshToken)
            .authentication(authentication)
            .build();

        dynamoDBMapper.save(itemToSave);
    }

    public OAuth2RefreshToken readRefreshToken(String token) {
        String tokenId = extractTokenKey(token);

        return Optional.ofNullable(dynamoDBMapper.load(OAuthRefreshToken.class, tokenId))
            .map(OAuthRefreshToken::getToken)
            .orElse(null);
    }

    public void removeRefreshToken(OAuth2RefreshToken token) {
        removeRefreshToken(token.getValue());
    }

    public void removeRefreshToken(String token) {
        String tokenId = extractTokenKey(token);
        OAuthRefreshToken itemToDelete = OAuthRefreshToken.builder().tokenId(tokenId).build();

        dynamoDBMapper.delete(itemToDelete);
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return readAuthenticationForRefreshToken(token.getValue());
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(String value) {
        String tokenId = extractTokenKey(value);

        return Optional.ofNullable(dynamoDBMapper.load(OAuthRefreshToken.class, tokenId))
            .map(OAuthRefreshToken::getAuthentication)
            .orElse(null);
    }

    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        removeAccessTokenUsingRefreshToken(refreshToken.getValue());
    }

    public void removeAccessTokenUsingRefreshToken(String refreshToken) {
        String refreshTokenId = extractTokenKey(refreshToken);

        DynamoDBQueryExpression query = new DynamoDBQueryExpression<OAuthAccessToken>()
            .withIndexName("refreshToken-index")
            .withConsistentRead(Boolean.FALSE)
            .withHashKeyValues(OAuthAccessToken.builder()
                .refreshToken(refreshTokenId)
                .build());

        List<OAuthAccessToken> accessTokens = dynamoDBMapper.query(OAuthAccessToken.class, query);

        dynamoDBMapper.batchDelete(accessTokens);
    }

    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        DynamoDBQueryExpression query = new DynamoDBQueryExpression<OAuthAccessToken>()
            .withIndexName("clientId-userName-index")
            .withConsistentRead(Boolean.FALSE)
            .withHashKeyValues(OAuthAccessToken.builder()
                .clientId(clientId)
                .build());

        List<OAuthAccessToken> accessTokens = dynamoDBMapper.query(OAuthAccessToken.class, query);
        return accessTokens.stream().map(OAuthAccessToken::getToken).collect(Collectors.toList());
    }

    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        DynamoDBQueryExpression query = new DynamoDBQueryExpression<OAuthAccessToken>()
            .withIndexName("clientId-userName-index")
            .withConsistentRead(Boolean.FALSE)
            .withHashKeyValues(OAuthAccessToken.builder()
                .clientId(clientId)
                .userName(userName)
                .build());

        List<OAuthAccessToken> accessTokens = dynamoDBMapper.query(OAuthAccessToken.class, query);
        return accessTokens.stream().map(OAuthAccessToken::getToken).collect(Collectors.toList());
    }

    protected String extractTokenKey(String value) {
        if (value == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        try {
            byte[] bytes = digest.digest(value.getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }
    }

}
