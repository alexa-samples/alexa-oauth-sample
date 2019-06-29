/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */

package com.oauth.server.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.oauth.server.dto.OAuthClientDetails;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.util.StringUtils;

/**
 * A DAO to access {@link ClientDetails} in DynamoDB.
 *
 * @author Lucun Cai
 */
@RequiredArgsConstructor
@Log4j2
public class DynamoDBClientDetailsDAO implements ClientDetailsService, ClientRegistrationService {

    private final DynamoDBMapper dynamoDBMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Load {@link ClientDetails} by clientId provided.
     *
     * @param clientId client id.
     * @return client details.
     * @throws NoSuchClientException if clientId not found.
     */
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws NoSuchClientException {
        return Optional.ofNullable(dynamoDBMapper.load(OAuthClientDetails.class, clientId))
            .map(OAuthClientDetails::toClientDetails)
            .orElseThrow(() -> new NoSuchClientException("Client: " + clientId + " not found."));
    }

    /**
     * Add a new {@link ClientDetails} into Database.
     *
     * @param clientDetails client details to be added.
     * @throws ClientAlreadyExistsException if client details already exists.
     */
    @Override
    public void addClientDetails(ClientDetails clientDetails) throws ClientAlreadyExistsException {

        OAuthClientDetails oAuthClientDetails = dynamoDBMapper.load(OAuthClientDetails.class,
            clientDetails.getClientId());

        if (oAuthClientDetails != null) {
            throw new ClientAlreadyExistsException("client already exists: " + clientDetails.getClientId());
        }

        addOrUpdateClientDetails(clientDetails);
    }

    /**
     * Update an existing {@link ClientDetails} in database.
     *
     * @param clientDetails client details.
     * @throws NoSuchClientException if client not exit.
     */
    @Override
    public void updateClientDetails(@NonNull ClientDetails clientDetails) throws NoSuchClientException {
        OAuthClientDetails oAuthClientDetails = dynamoDBMapper.load(OAuthClientDetails.class,
            clientDetails.getClientId());

        if (oAuthClientDetails == null) {
            throw new NoSuchClientException("client not exists: " + clientDetails.getClientId());
        }

        addOrUpdateClientDetails(clientDetails);
    }

    /**
     * Add or update a client details in database.
     *
     * @param clientDetails client details.
     */
    public void addOrUpdateClientDetails(@NonNull ClientDetails clientDetails) {
        List<String> autoApproveList = clientDetails.getScope().stream()
            .filter(scope -> clientDetails.isAutoApprove(scope))
            .collect(Collectors.toList());

        OAuthClientDetails oAuthClientDetails = OAuthClientDetails
            .builder()
            .clientId(clientDetails.getClientId())
            .authorities(StringUtils.collectionToCommaDelimitedString(clientDetails.getAuthorities()))
            .authorizedGrantTypes(
                StringUtils.collectionToCommaDelimitedString(clientDetails.getAuthorizedGrantTypes()))
            .scopes(StringUtils.collectionToCommaDelimitedString(clientDetails.getScope()))
            .webServerRedirectUri(
                StringUtils.collectionToCommaDelimitedString(clientDetails.getRegisteredRedirectUri()))
            .accessTokenValidity(clientDetails.getAccessTokenValiditySeconds())
            .refreshTokenValidity(clientDetails.getRefreshTokenValiditySeconds())
            .autoapprove(StringUtils.collectionToCommaDelimitedString(autoApproveList))
            .build();

        DynamoDBMapperConfig dynamoDBMapperConfig = DynamoDBMapperConfig
            .builder()
            .withSaveBehavior(
                SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build();

        dynamoDBMapper.save(oAuthClientDetails, dynamoDBMapperConfig);
    }

    /**
     * Update the client secret for a specific client id.
     *
     * @param clientId client id.
     * @param secret client secret.
     * @throws NoSuchClientException if client not exist.
     */
    @Override
    public void updateClientSecret(@NonNull String clientId, @NonNull String secret) throws NoSuchClientException {
        OAuthClientDetails oAuthClientDetails = dynamoDBMapper.load(OAuthClientDetails.class, clientId);

        if (oAuthClientDetails == null) {
            throw new NoSuchClientException("client not exists: " + clientId);
        }

        OAuthClientDetails updatedItem = oAuthClientDetails.toBuilder().clientSecret(passwordEncoder.encode(secret))
            .build();
        dynamoDBMapper.save(updatedItem);
    }

    /**
     * Remove a specific client details by clientId.
     *
     * @param clientId client id.
     */
    @Override
    public void removeClientDetails(@NonNull String clientId) {
        OAuthClientDetails oAuthClientDetails = dynamoDBMapper.load(OAuthClientDetails.class, clientId);

        if (oAuthClientDetails == null) {
            log.error("clientId {} already deleted.", clientId);
        } else {
            dynamoDBMapper.delete(oAuthClientDetails);
        }
    }

    /**
     * List all the oauth clients in database by scanning the database.
     *
     * @return all client details.
     */
    @Override
    public List<ClientDetails> listClientDetails() {
        return dynamoDBMapper.scan(OAuthClientDetails.class, new DynamoDBScanExpression())
            .stream()
            .map(OAuthClientDetails::toClientDetails)
            .collect(Collectors.toList());
    }
}
