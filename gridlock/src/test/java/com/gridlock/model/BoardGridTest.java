package com.gridlock.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoardGridTest {
    @Test
    public void adjacentEnhancersMultiplyProductionAndAugmentsIncreaseBuildingLevels() {
        BoardGrid board = new BoardGrid(3, 3);
        Card production = new Card(CardType.PRODUCTION);
        Card enhancer = new Card(CardType.ENHANCER);

        assertTrue(board.placeCard(production, 1, 1));
        assertTrue(board.placeCard(enhancer, 1, 2));
        assertEquals(1.5, productionAt(board, production, 1, 1), 0.0001);

        enhancer.upgrade();
        production.upgrade();
        assertEquals(4.0, productionAt(board, production, 1, 1), 0.0001);
    }

    @Test
    public void placementRejectsOccupiedSlotsAndNonBuildingCards() {
        BoardGrid board = new BoardGrid(3, 3);

        assertTrue(board.placeCard(new Card(CardType.PRODUCTION), 0, 0));
        assertFalse(board.placeCard(new Card(CardType.ENHANCER), 0, 0));
        assertFalse(board.placeCard(new Card(CardType.AUGMENT), 0, 1));
    }

    private double productionAt(BoardGrid board, Card card, int row, int column) {
        BoardSlot slot = board.getSlot(row, column);
        return card.productionPerSecond(
                new EffectContext(null, slot, board.getAdjacentSlots(slot)));
    }
}
