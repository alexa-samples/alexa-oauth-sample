/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.authentication;

/**
 * An Enum represents the authentication roles for users and clients.
 *
 * @author Lucun Cai
 */
public enum RoleEnum {
    ROLE_USER_ADMIN, //A role for administrators to manage clients and partners.
    ROLE_CLIENT_ADMIN //A role for an internal administration OAuth client.
}
