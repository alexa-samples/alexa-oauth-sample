/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.util.StringUtils;

/**
 * An DTO object represents an OAuth client.
 *
 * @author Lucun Cai
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "OAuthClientDetails")
public class OAuthClientDetails {
    private static final String RESOURCE_ID = "oauth2-resource";

    @DynamoDBHashKey
    String clientId;

    String clientSecret;

    String scopes;

    String authorizedGrantTypes;

    String webServerRedirectUri;

    String authorities;

    Integer accessTokenValidity;

    Integer refreshTokenValidity;

    String autoapprove;

    public ClientDetails toClientDetails() {
        BaseClientDetails clientDetails = new BaseClientDetails(clientId, RESOURCE_ID, scopes, authorizedGrantTypes, authorities, webServerRedirectUri);
        clientDetails.setClientSecret(clientSecret);
        clientDetails.setAccessTokenValiditySeconds(accessTokenValidity);
        clientDetails.setRefreshTokenValiditySeconds(refreshTokenValidity);
        clientDetails.setAutoApproveScopes(StringUtils
            .commaDelimitedListToSet(autoapprove));

        return clientDetails;
    }
}
