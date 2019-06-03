/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.configuration;

import com.oauth.server.authentication.RoleEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * Configuration for Resource APIs.
 *
 * @author Lucun Cai
 */
@EnableResourceServer
@Configuration
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/api/**")
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/partner/token").hasAuthority(RoleEnum.ROLE_CLIENT_ADMIN.name())
            .antMatchers("/api/**").authenticated();
    }
}
