package com.jung.planet.user.entity;

import lombok.Getter;

@Getter
public enum SubscriptionType {
    BASIC(3, false),
    PREMIUM(6, true);

    private final int maxPlants;
    private final boolean aiServiceAccess;

    SubscriptionType(int maxPlants, boolean aiServiceAccess) {
        this.maxPlants = maxPlants;
        this.aiServiceAccess = aiServiceAccess;
    }
}
