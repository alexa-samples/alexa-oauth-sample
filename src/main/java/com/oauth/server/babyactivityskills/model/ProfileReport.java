/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.babyactivityskills.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a ProfileReport that is sent to Alexa.
 *
 * See https://developer.amazon.com/docs/health/profiles.html for more details.
 */
public class ProfileReport {
    private final String messageId;
    private final Set<Profile> profiles;

    @JsonCreator
    public ProfileReport(@JsonProperty("messageId")String messageId,
                         @JsonProperty("profiles")Set<Profile> profiles) {
        this.messageId = messageId;
        this.profiles = profiles;
    }

    public String getMessageId() {
        return messageId;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileReport that = (ProfileReport) o;
        return Objects.equals(messageId, that.messageId) &&
                Objects.equals(profiles, that.profiles);
    }

    @Override
    public int hashCode() {

        return Objects.hash(messageId, profiles);
    }
}
