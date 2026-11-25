package com.gridlock.model;

public enum CardType {
    PRODUCTION("Production Building", "Produces 1 production per turn and level.", 1),
    ENHANCER("Enhancement Building", "Adds 50% production to adjacent buildings per level.", 0),
    AUGMENT("Augment", "Adds one level to a building on the board.", 0),
    OVERCLOCK("Overclock", "Immediately adds current factory production to this round.", 0);

    private final String displayName;
    private final String description;
    private final int baseProduction;

    CardType(String displayName, String description, int baseProduction) {
        this.displayName = displayName;
        this.description = description;
        this.baseProduction = baseProduction;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getBaseProduction() {
        return baseProduction;
    }
}
