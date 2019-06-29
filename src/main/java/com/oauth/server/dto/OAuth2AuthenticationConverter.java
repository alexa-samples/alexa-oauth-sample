/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.amazonaws.util.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * This is a DynamoDBTypeConverter that converts between OAuth2Authentication to String.
 *
 * @author Lucun Cai
 */
public class OAuth2AuthenticationConverter implements DynamoDBTypeConverter<String, OAuth2Authentication> {

    @Override
    public String convert(final OAuth2Authentication authentication) {
        byte[] bytes = SerializationUtils.serialize(authentication);
        return new String(Base64.encode(bytes));
    }

    @Override
    public OAuth2Authentication unconvert(final String authenticationString) {
        byte[] bytes = Base64.decode(authenticationString.getBytes());
        return SerializationUtils.deserialize(bytes);
    }
}
