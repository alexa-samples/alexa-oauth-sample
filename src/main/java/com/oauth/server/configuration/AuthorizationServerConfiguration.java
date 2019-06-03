/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.configuration;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.oauth.server.dao.DynamoDBClientDetailsDAO;
import com.oauth.server.dao.DynamoDBPartnerTokenDAO;
import com.oauth.server.dao.DynamoDBTokenDAO;
import com.oauth.server.authentication.AuthenticationServiceProvider;
import com.oauth.server.dao.DynamoDBAuthorizationCodeDAO;
import com.oauth.server.dao.DynamoDBPartnerDetailsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.token.ClientTokenServices;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * Configuration for authorization server.
 *
 * @author Lucun Cai
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Bean
    public AuthenticationServiceProvider authenticationServiceProvider() {
        return new AuthenticationServiceProvider(passwordEncoder());
    }

    @Bean
    @Scope(proxyMode = ScopedProxyMode.INTERFACES)
    public ClientTokenServices clientTokenServices() {
        return new DynamoDBPartnerTokenDAO(dynamoDBMapper);
    }

    @Bean
    @Scope(proxyMode = ScopedProxyMode.INTERFACES)
    public TokenStore tokenStore() {
        return new DynamoDBTokenDAO(dynamoDBMapper);
    }

    @Bean
    @Scope(proxyMode = ScopedProxyMode.INTERFACES)
    public ApprovalStore approvalStore() {
        TokenApprovalStore approvalStore = new TokenApprovalStore();
        approvalStore.setTokenStore(tokenStore());
        return approvalStore;
    }

    @Bean
    @Scope(proxyMode = ScopedProxyMode.INTERFACES)
    public AuthorizationCodeServices authorizationCodeServices() {
        return new DynamoDBAuthorizationCodeDAO(dynamoDBMapper);
    }

    @Bean
    public DynamoDBClientDetailsDAO dynamoDBClientDetailsService() {
        return new DynamoDBClientDetailsDAO(dynamoDBMapper, passwordEncoder());
    }

    @Bean
    public DynamoDBPartnerDetailsDAO dynamoDBPartnerDetailsService() {
        return new DynamoDBPartnerDetailsDAO(dynamoDBMapper);
    }

    @Bean
    public DynamoDBPartnerTokenDAO dynamoDBPartnerTokenService() {
        return new DynamoDBPartnerTokenDAO(dynamoDBMapper);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(dynamoDBClientDetailsService());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.allowFormAuthenticationForClients();
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
            .approvalStore(approvalStore())
            .authorizationCodeServices(authorizationCodeServices())
            .tokenStore(tokenStore())
            .authenticationManager(authenticationServiceProvider())
            .userDetailsService(authenticationServiceProvider());
    }
}