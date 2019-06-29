/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.authentication;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.apache.http.auth.BasicUserPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.security.Principal;

/**
 * A simple User-ID based authentication token.
 *
 * @author Lucun Cai
 */
public class UserIDAuthenticationToken extends AbstractAuthenticationToken {

    @NonNull
    private String userID;

    public UserIDAuthenticationToken(final String userID) {
        super(ImmutableList.of());
        this.userID = userID;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Principal getPrincipal() {
        return new BasicUserPrincipal(userID);
    }
}
