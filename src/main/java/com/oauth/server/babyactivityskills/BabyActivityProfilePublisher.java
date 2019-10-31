/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.babyactivityskills;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.oauth.server.authentication.PartnerTokenManager;
import com.oauth.server.babyactivityskills.model.Capability;
import com.oauth.server.babyactivityskills.model.Name;
import com.oauth.server.babyactivityskills.model.Profile;
import com.oauth.server.babyactivityskills.model.ProfileReport;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Responsible for publishing baby activity profiles for a user on behalf of a partner to Alexa.
 */
public class BabyActivityProfilePublisher {
    private static final Logger log = LoggerFactory.getLogger(BabyActivityProfilePublisher.class);

    private static final String PROFILE_ENDPOINT = "https://api.amazonalexa.com/v1/health/profile";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, Set<Profile>> MOCK_BABY_PROFILES = new HashMap<>();

    static {
        MOCK_BABY_PROFILES.put("user",
                ImmutableSet.of(
                        new Profile("user/baby-1",
                                new Name("Maggie", "Simpson", ImmutableSet.of("Maggie", "Mag")),
                                ImmutableSet.of(Capability.WEIGHT, Capability.DIAPER_CHANGE, Capability.INFANT_FEEDING, Capability.SLEEP)))
        );

        MOCK_BABY_PROFILES.put("admin",
                ImmutableSet.of(new Profile("admin/baby-1",
                        new Name("James", "Kirk", ImmutableSet.of("jim")),
                        ImmutableSet.of(Capability.WEIGHT, Capability.DIAPER_CHANGE, Capability.INFANT_FEEDING, Capability.SLEEP)))
        );
    }

    private final TokenStore tokenStore;

    private final PartnerTokenManager partnerTokenManager;

    private final HttpClientFactory httpClientFactory;

    private final ExecutorService asyncExecutor;

    public BabyActivityProfilePublisher(final TokenStore tokenStore,
                                        final PartnerTokenManager partnerTokenManager,
                                        final HttpClientFactory httpClientFactory,
                                        final ExecutorService asyncExecutor) {
        this.tokenStore = tokenStore;
        this.partnerTokenManager = partnerTokenManager;
        this.httpClientFactory = httpClientFactory;
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * Publish profile given a client access token that was provided by sample Oauth Server to access resources
     * corresponding to a user and partner who has been granted access to call Alexa. The code resolves the
     * userId corresponding to accessToken, constructs and publishes the profile using partner's LWA access token.
     * <p>
     * See: https://developer.amazon.com/docs/health/profiles.html#how-to-send-a-profile-report for more details.
     *
     * @param clientAccessToken - AccessToken provided by Oauth Server.
     * @param partnerId         - Alexa PartnerId
     */
    public void publishProfiles(final String clientAccessToken, final String partnerId) {
        final String userId = getUserFromAccessToken(clientAccessToken);
        final OAuth2AccessToken lwaAccessToken = partnerTokenManager.getAccessToken(userId, partnerId);
        publishProfilesForUser(userId, lwaAccessToken);
    }

    public void publishProfilesAsync(final String clientAccessToken, final String partnerId) {
        asyncExecutor.execute(() -> publishProfiles(clientAccessToken, partnerId));
    }


    private void publishProfilesForUser(final String userId, final OAuth2AccessToken lwaAccessToken) {
        final Set<Profile> profiles = MOCK_BABY_PROFILES.get(userId);
        if (profiles == null) {
            throw new RuntimeException("No profiles found for user");
        }
        final HttpPost postRequest = newPostProfilesRequest(userId, profiles, lwaAccessToken.getValue());

        try (final CloseableHttpClient httpClient = httpClientFactory.createDefault();
             final CloseableHttpResponse response = httpClient.execute(postRequest)) {
            log.info("Posted {} profiles for user:{} and received response:{}", profiles.size(), userId, response.getStatusLine());
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed to publish baby profiles for user. ResponseStatus:" + response.getStatusLine());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish baby profiles for user:" + userId, e);
        }
    }

    private HttpPost newPostProfilesRequest(final String userId,
                                           final Set<Profile> profiles, final String lwaAccessToken) {
        final HttpPost postRequest = new HttpPost(PROFILE_ENDPOINT);
        postRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + lwaAccessToken);
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        try {
            final ProfileReport profileReport = new ProfileReport(UUID.randomUUID().toString(), profiles);
            final String requestPayload = MAPPER.writeValueAsString(Collections.singletonMap("report", profileReport));
            final StringEntity entity = new StringEntity(requestPayload);
            entity.setContentType(ContentType.APPLICATION_JSON.toString());
            postRequest.setEntity(entity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to compose post request to send profiles for user:" + userId);
        }
        return postRequest;
    }

    private String getUserFromAccessToken(final String accessToken) {
        final OAuth2Authentication authentication = tokenStore.readAuthentication(accessToken);
        if (authentication == null) {
            throw new IllegalArgumentException("No resource found for access token.");
        }
        return authentication.getName();
    }

}
