package com.gridlock.model;

public interface CardEffectStrategy {
    double calculateProductionMultiplier(Card card, EffectContext context);
}
