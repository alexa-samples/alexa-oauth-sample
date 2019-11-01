/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.babyactivityskills.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

/**
 * Represents Alexa Health Capabilities.
 *
 * See: https://developer.amazon.com/docs/health/profiles.html , 'Capability details' section for more info.
 */
public class Capability {

    // baby activity operations
    private static final String ADD = "Add";
    private static final String DELETE = "Delete";
    private static final String GET = "Get";
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final String CANCEL = "Cancel";
    private static final String PAUSE = "Pause";
    private static final String RESUME = "Resume";
    private static final String SWITCH = "Switch";

    // available baby activity skill capabilities
    public static Capability WEIGHT = newBabyActivityCapability("Weight",
            ImmutableSet.of(ADD, DELETE, GET));
    public static Capability SLEEP = newBabyActivityCapability("Sleep",
            ImmutableSet.of(ADD, DELETE, GET, START, STOP, CANCEL, RESUME, PAUSE));
    public static Capability DIAPER_CHANGE = newBabyActivityCapability("DiaperChange",
            ImmutableSet.of(ADD, DELETE, GET));
    public static Capability INFANT_FEEDING = newBabyActivityCapability("InfantFeeding",
            ImmutableSet.of(ADD, DELETE, GET, START, STOP, CANCEL, RESUME, PAUSE, SWITCH));

    private static Capability newBabyActivityCapability(final String name, final Set<String> supportedOperations) {
        return new Capability("Alexa.Health." + name, "AlexaInterface", "1", supportedOperations);
    }

    @JsonProperty
    private final String name;
    @JsonProperty
    private final String type;
    @JsonProperty
    private final String version;
    @JsonProperty
    private final Set<String> supportedOperations;

    @JsonCreator
    public Capability(@JsonProperty("name") String name,
                      @JsonProperty("type")String type,
                      @JsonProperty("version")String version,
                      @JsonProperty("supportedOperations")Set<String> supportedOperations) {
        this.name = name;
        this.type = type;
        this.version = version;
        this.supportedOperations = supportedOperations;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public Set<String> getSupportedOperations() {
        return supportedOperations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capability that = (Capability) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(version, that.version) &&
                Objects.equals(supportedOperations, that.supportedOperations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, type, version, supportedOperations);
    }
}
