package com.gridlock.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BoardGrid {
    private final int rows;
    private final int columns;
    private final BoardSlot[][] slots;

    public BoardGrid(int rows, int columns) {
        if (rows < 1 || columns < 1) {
            throw new IllegalArgumentException("Board dimensions must be positive.");
        }
        this.rows = rows;
        this.columns = columns;
        this.slots = new BoardSlot[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                slots[row][column] = new BoardSlot(row, column);
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public BoardSlot getSlot(int row, int column) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            throw new IndexOutOfBoundsException("Slot is outside the board.");
        }
        return slots[row][column];
    }

    public List<BoardSlot> getSlots() {
        List<BoardSlot> allSlots = new ArrayList<>();
        for (BoardSlot[] row : slots) {
            Collections.addAll(allSlots, row);
        }
        return Collections.unmodifiableList(allSlots);
    }

    public List<BoardSlot> getAdjacentSlots(BoardSlot slot) {
        List<BoardSlot> neighbors = new ArrayList<>(4);
        addIfInBounds(neighbors, slot.getRow() - 1, slot.getColumn());
        addIfInBounds(neighbors, slot.getRow() + 1, slot.getColumn());
        addIfInBounds(neighbors, slot.getRow(), slot.getColumn() - 1);
        addIfInBounds(neighbors, slot.getRow(), slot.getColumn() + 1);
        return Collections.unmodifiableList(neighbors);
    }

    public boolean placeCard(Card card, int row, int column) {
        BoardSlot slot = getSlot(row, column);
        if (slot.isOccupied() || !isBuilding(card)) {
            return false;
        }
        slot.setCard(card);
        return true;
    }

    public boolean augmentCard(int row, int column) {
        BoardSlot slot = getSlot(row, column);
        if (!slot.isOccupied() || !isBuilding(slot.getCard())) {
            return false;
        }
        slot.getCard().upgrade();
        return true;
    }

    public Card removeCard(int row, int column) {
        BoardSlot slot = getSlot(row, column);
        Card card = slot.getCard();
        slot.setCard(null);
        return card;
    }

    public List<Card> clear() {
        List<Card> cards = new ArrayList<>();
        for (BoardSlot[] row : slots) {
            for (BoardSlot slot : row) {
                if (slot.getCard() != null) {
                    cards.add(removeCard(slot.getRow(), slot.getColumn()));
                }
            }
        }
        return cards;
    }

    public double calculateSynergyMultiplier(BoardSlot slot) {
        double multiplier = 1;
        for (BoardSlot neighbor : getAdjacentSlots(slot)) {
            Card card = neighbor.getCard();
            if (card != null && card.getType() == CardType.ENHANCER) {
                multiplier += card.getEnhancementBonus();
            }
        }
        return multiplier;
    }

    private void addIfInBounds(List<BoardSlot> neighbors, int row, int column) {
        if (row >= 0 && row < rows && column >= 0 && column < columns) {
            neighbors.add(slots[row][column]);
        }
    }

    private boolean isBuilding(Card card) {
        return card != null
                && (card.getType() == CardType.PRODUCTION || card.getType() == CardType.ENHANCER);
    }
}
