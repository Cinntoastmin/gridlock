package com.gridlock.model;

import java.util.Collections;
import java.util.List;

public final class EffectContext {
    private final GameState gameState;
    private final BoardSlot slot;
    private final List<BoardSlot> neighbors;

    public EffectContext(GameState gameState, BoardSlot slot, List<BoardSlot> neighbors) {
        this.gameState = gameState;
        this.slot = slot;
        this.neighbors = Collections.unmodifiableList(neighbors);
    }

    public GameState getGameState() {
        return gameState;
    }

    public BoardSlot getSlot() {
        return slot;
    }

    public List<BoardSlot> getNeighbors() {
        return neighbors;
    }
}
