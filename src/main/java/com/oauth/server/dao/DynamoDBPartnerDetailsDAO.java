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
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * A DAO to access {@link OAuthPartner} in DynamoDB.
 *
 * @author Lucun Cai
 */
@Log4j2
public class DynamoDBPartnerDetailsDAO {

    private DynamoDBMapper dynamoDBMapper;

    public DynamoDBPartnerDetailsDAO(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    /**
     * Returns an OAuthPartner object whose keys match those of the prototype key object given, or null if no such item exists.
     *
     * @param partnerId partnerId.
     * @return {@link OAuthPartner} or null if not found.
     */
    public OAuthPartner loadPartnerByPartnerId(@NonNull String partnerId) {
        return dynamoDBMapper.load(OAuthPartner.class, partnerId);
    }

    /**
     * Scans through an Amazon DynamoDB table and returns the matching results as an unmodifiable list of instantiated objects.
     *
     * @return a list of {@link OAuthPartner}.
     */
    public List<OAuthPartner> listPartners() {
        return dynamoDBMapper.scan(OAuthPartner.class, new DynamoDBScanExpression());
    }

    /**
     * Save the {@link OAuthPartner} provided.
     *
     * @param partner {@link OAuthPartner}
     */
    public void savePartner(OAuthPartner partner) {
        dynamoDBMapper.save(partner);
    }

    /**
     * Delete the {@link OAuthPartner} by partnerId.
     *
     * @param partnerId
     */
    public void deletePartnerByPartnerId(@NonNull String partnerId) {
        OAuthPartner partner = dynamoDBMapper.load(OAuthPartner.class, partnerId);
        if (partner == null) {
            log.error("partner {} already deleted.", partnerId);
        } else {
            dynamoDBMapper.delete(partner);
        }
    }
}
