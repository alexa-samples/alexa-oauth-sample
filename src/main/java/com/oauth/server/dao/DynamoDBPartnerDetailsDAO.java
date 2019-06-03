/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.oauth.server.dto.OAuthPartner;
import java.util.List;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;

/**
 * A DAO to access {@link OAuthPartner} in DynamoDB.
 *
 * @author Lucun Cai
 */
public class DynamoDBPartnerDetailsDAO {

    private DynamoDBMapper dynamoDBMapper;

    public DynamoDBPartnerDetailsDAO(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public OAuthPartner loadPartnerByPartnerId(String partnerId) {
        return dynamoDBMapper.load(OAuthPartner.class, partnerId);
    }

    public List<OAuthPartner> listPartners() {
        return dynamoDBMapper.scan(OAuthPartner.class, new DynamoDBScanExpression());
    }

    public void savePartner(OAuthPartner partner) throws ClientAlreadyExistsException {
        dynamoDBMapper.save(partner);
    }

    public void deletePartnerByPartnerId(String partnerId) throws ClientAlreadyExistsException {
        dynamoDBMapper.delete(OAuthPartner.builder().partnerId(partnerId).build());
    }
}
