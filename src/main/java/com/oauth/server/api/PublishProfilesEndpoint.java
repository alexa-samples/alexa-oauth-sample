/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.api;

import com.oauth.server.babyactivityskills.BabyActivityProfilePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * RestController to publish baby activity profiles for a user.
 *
 */
@RestController
public class PublishProfilesEndpoint {

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private BabyActivityProfilePublisher babyActivityProfilePublisher;

    /**
     * Publishes baby profiles for a user to Alexa on behalf of Alexa Partner.
    */
    @RequestMapping(value = "/api/profiles/publish", method = RequestMethod.POST)
    public void publishProfiles(final @RequestBody @RequestParam Map<String, String> parameters) {
        String partnerId = parameters.get("partner_id");
        String clientAccessToken = new BearerTokenExtractor().extract(context).getName();
        if (clientAccessToken == null) {
            throw new IllegalArgumentException("Bearer Token must be provided.");
        }
        if (partnerId == null) {
            throw new IllegalArgumentException("PartnerId parameter must be specified.");
        }
        babyActivityProfilePublisher.publishProfiles(clientAccessToken, partnerId);
    }

}
