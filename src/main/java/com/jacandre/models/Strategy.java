package com.jacandre.models;

// Defines the possible strategies an agent can adopt.
public enum Strategy {
    /**
     * The Helper strategy: Spends energy (ASSIST_COST) to assist low-fitness neighbours
     * in exchange for a share of the FOOD_REWARD (SHARE_RATE).
     */
    HELPER,

    /**
     * The Selfish strategy: Does not offer assistance to neighbours.
     * Only pursues resources for itself.
     */
    SELFISH
}
