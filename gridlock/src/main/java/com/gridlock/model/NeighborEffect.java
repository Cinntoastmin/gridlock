package com.gridlock.model;

public final class NeighborEffect implements CardEffectStrategy {
    @Override
    public double calculateProductionMultiplier(Card card, EffectContext context) {
        double multiplier = 1;
        for (BoardSlot neighbor : context.getNeighbors()) {
            Card neighborCard = neighbor.getCard();
            if (neighborCard != null && neighborCard.getType() == CardType.ENHANCER) {
                multiplier += neighborCard.getEnhancementBonus();
            }
        }
        return multiplier;
    }
}
