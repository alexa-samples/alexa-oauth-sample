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
 * Represents a Health Profile.
 * <p>
 * See https://developer.amazon.com/docs/health/profiles.html for more details.
 */
public class Profile {
    private final String profileId;
    private final Name name;
    private final Set<Capability> capabilities;

    @JsonCreator
    public Profile(@JsonProperty("profileId") final String profileId,
                   @JsonProperty("name") final Name babyName,
                   @JsonProperty("capabilities") final Set<Capability> capabilities) {
        this.profileId = profileId;
        this.name = babyName;
        this.capabilities = capabilities;
    }

    public String getProfileId() {
        return profileId;
    }

    public Name getName() {
        return name;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(profileId, profile.profileId) &&
                Objects.equals(name, profile.name) &&
                Objects.equals(capabilities, profile.capabilities);
    }

    @Override
    public int hashCode() {

        return Objects.hash(profileId, name, capabilities);
    }
}
