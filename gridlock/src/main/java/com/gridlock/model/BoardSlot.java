package com.gridlock.model;

public final class BoardSlot {
    private final int row;
    private final int column;
    private Card card;

    BoardSlot(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Card getCard() {
        return card;
    }

    public boolean isOccupied() {
        return card != null;
    }

    void setCard(Card card) {
        this.card = card;
    }
}
