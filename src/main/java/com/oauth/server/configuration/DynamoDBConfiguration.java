/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.configuration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AWS DynamoDB Client.
 *
 * @author Lucun Cai
 */
@Configuration
public class DynamoDBConfiguration {

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                   .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                   .build();
    }

    @Bean
    public DynamoDBMapper getDynamoDBMapper() {
        AmazonDynamoDB amazonDynamoDB = amazonDynamoDB();
        return new DynamoDBMapper(amazonDynamoDB);
    }
}
