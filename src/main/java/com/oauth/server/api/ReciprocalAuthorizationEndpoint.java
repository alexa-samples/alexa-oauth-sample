/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.api;

import com.oauth.server.dao.DynamoDBPartnerTokenDAO;
import com.oauth.server.dto.OAuthPartner;
import com.oauth.server.dao.DynamoDBPartnerDetailsDAO;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UnsupportedGrantTypeException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for reciprocal authorization endpoint.
 *
 * <p>
 * This endpoint is used for reciprocal account linking by Alexa, and will be invoked with authorization codes that can be
 * exchanged for access tokens (from other OAuth server).
 * This endpoint is introduced by Alexa but can also be used by other OAuth servers.
 * https://w.amazon.com/index.php/Alexa%20Skills%20Kit/Permissions%20Framework/Reciprocal%20Authorization/SPI
 * </p>
 *
 * @author Lucun Cai
 */
@RestController
public class ReciprocalAuthorizationEndpoint {

    private static final String GRANT_TYPE = "reciprocal_authorization_code";

    @Autowired
    private DynamoDBPartnerTokenDAO partnerTokenRepository;

    @Autowired
    private DynamoDBPartnerDetailsDAO partnerDetailsRepository;

    @RequestMapping(value = "/api/reciprocal/authorize", method = RequestMethod.POST)
    public void postReciprocalCode(final @RequestBody @RequestParam Map<String, String> parameters) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String grantType = parameters.get("grant_type");
        // It is not the client_id we got from partner, but the client id we vend out to partner (partnerId).
        String partnerId = parameters.get("client_id");
        String authorizationCode = parameters.get("code");

        if (!StringUtils.equals(grantType, GRANT_TYPE)) {
            throw new UnsupportedGrantTypeException("Only reciprocal_authorization_code is supported in this endpoint");
        }

        OAuthPartner partner = partnerDetailsRepository.loadPartnerByPartnerId(partnerId);

        if (partner == null) {
            throw new NoSuchClientException("Invalid partner id: " + partnerId);
        }

        OAuth2ProtectedResourceDetails resourceDetails = partner.toProtectedResourceDetails();

        AuthorizationCodeAccessTokenProvider tokenProvider = new AuthorizationCodeAccessTokenProvider();
        tokenProvider.setStateMandatory(false);

        OAuth2AccessToken accessToken = tokenProvider.obtainAccessToken(resourceDetails,
            createAccessTokenRequest(authorizationCode));

        partnerTokenRepository.removeAccessToken(resourceDetails, auth);
        partnerTokenRepository.saveAccessToken(resourceDetails, auth, accessToken);
    }

    /**
     * Method to construct AccessTokenRequest.
     *
     * @param authorizationCode authorization code.
     * @return AccessTokenRequest.
     */
    private AccessTokenRequest createAccessTokenRequest(String authorizationCode) {
        AccessTokenRequest accessTokenRequest = new DefaultAccessTokenRequest();
        accessTokenRequest.setAuthorizationCode(authorizationCode);
        return accessTokenRequest;
    }

}
