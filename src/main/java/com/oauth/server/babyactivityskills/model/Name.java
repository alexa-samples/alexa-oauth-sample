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
 * Represents 'name' object in health profile report.
 *
 * See: https://developer.amazon.com/docs/health/profiles.html for more dertails.
 */
public class Name {
    private final String firstName;
    private final String lastName;
    private final Set<String> nickNames;

    @JsonCreator
    public Name( @JsonProperty("firstName") String firstName,
                 @JsonProperty("lastName")String lastName,
                 @JsonProperty("nickNames")Set<String> nickNames) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickNames = nickNames;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Set<String> getNickNames() {
        return nickNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name name = (Name) o;
        return Objects.equals(firstName, name.firstName) &&
                Objects.equals(lastName, name.lastName) &&
                Objects.equals(nickNames, name.nickNames);
    }

    @Override
    public int hashCode() {

        return Objects.hash(firstName, lastName, nickNames);
    }
}
