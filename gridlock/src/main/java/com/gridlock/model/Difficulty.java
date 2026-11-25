package com.gridlock.model;

public enum Difficulty {
    EASY("Easy", 1.5, 2),
    MEDIUM("Medium", 2.0, 1),
    HARD("Hard", 2.5, 2);

    private final String displayName;
    private final double quotaMultiplier;
    private final int nextRoundTurns;

    Difficulty(String displayName, double quotaMultiplier, int nextRoundTurns) {
        this.displayName = displayName;
        this.quotaMultiplier = quotaMultiplier;
        this.nextRoundTurns = nextRoundTurns;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getQuotaMultiplier() {
        return quotaMultiplier;
    }

    public int getNextRoundTurns() {
        return nextRoundTurns;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
