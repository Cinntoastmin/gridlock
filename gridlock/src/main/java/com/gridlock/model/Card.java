package com.gridlock.model;

import java.util.UUID;

public final class Card {
    private final UUID id;
    private final CardType type;
    private final CardEffectStrategy effect;
    private int level;

    public Card(CardType type) {
        this(UUID.randomUUID(), type, 1);
    }

    private Card(UUID id, CardType type, int level) {
        this.id = id;
        this.type = type;
        this.level = level;
        this.effect = type == CardType.PRODUCTION ? new NeighborEffect() : new IdleBonusEffect();
    }

    public UUID getId() {
        return id;
    }

    public CardType getType() {
        return type;
    }

    public String getName() {
        return type.getDisplayName();
    }

    public int getLevel() {
        return level;
    }

    public int getBaseProduction() {
        return type.getBaseProduction() * level;
    }

    public double productionPerSecond(EffectContext context) {
        return getBaseProduction() * effect.calculateProductionMultiplier(this, context);
    }

    public void upgrade() {
        if (type == CardType.AUGMENT || type == CardType.OVERCLOCK) {
            throw new IllegalStateException("Only building cards can be upgraded.");
        }
        level++;
    }

    public double getEnhancementBonus() {
        return type == CardType.ENHANCER ? 0.5 * level : 0;
    }

    @Override
    public String toString() {
        return getName() + " (Lv. " + level + ")";
    }
}
