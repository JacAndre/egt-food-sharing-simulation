package com.jacandre.models;

import com.jacandre.core.Constants;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Food implements GridEntity {
    private final String id;
    private final double energyValue;
    private int age;

    public Food() {
        this.id = UUID.randomUUID().toString();
        this.energyValue = Constants.FOOD_REWARD;
        this.age = 0;
    }

    public void ageOneTick() {
        age++;
    }

    public boolean isExpired() {
        return age > Constants.FOOD_LIFESPAN;
    }
}

