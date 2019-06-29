/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * An DTO object represents an OAuth partner token.
 *
 * @author Lucun Cai
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "OAuthPartnerToken")
public class OAuthPartnerToken {

    @DynamoDBHashKey
    String tokenId;

    @DynamoDBTypeConvertedJson
    OAuth2AccessToken token;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "authenticationId-index")
    String authenticationId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "clientId-userName-index")
    String clientId;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "clientId-userName-index")
    String userName;
}
