package com.gridlock.model;

public final class IdleBonusEffect implements CardEffectStrategy {
    @Override
    public double calculateProductionMultiplier(Card card, EffectContext context) {
        return 1;
    }
}
