/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */

package com.oauth.server.api;

import com.oauth.server.authentication.PartnerTokenManager;
import com.oauth.server.dao.DynamoDBPartnerTokenDAO;
import com.oauth.server.dto.OAuthPartner;
import com.oauth.server.authentication.UserIDAuthenticationToken;
import com.oauth.server.dao.DynamoDBPartnerDetailsDAO;
import java.util.Map;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for partner token endpoint.
 *
 * <p>
 * This endpoint is called by admin clients to retrieve access tokens received from partner OAuth providers (e.g. LWA).
 * <p>
 *
 * @author Lucun Cai
 */
@RestController
public class PartnerTokenEndpoint {

    @Autowired
    private PartnerTokenManager partnerTokenManager;

    /**
     * Endpoint to retrieve a client token from ClientTokenService.
     */
    @RequestMapping(value = "/api/partner/token")
    public OAuth2AccessToken getPartnerToken(final @RequestParam Map<String, String> parameters) {
        final String userID = parameters.get("user_id");
        final String partnerId = parameters.get("partner_id");

        return partnerTokenManager.getAccessToken(userID, partnerId);
    }
}