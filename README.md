# Gridlock

Gridlock is a turn-based factory-building card game implemented with JavaFX.

## Run

From the `gridlock` directory, run:

```text
mvn javafx:run
```

## Play

- Choose a difficulty and click any cards to mulligan, then click **Start turn**.
- Select a building card and click an empty factory slot to place it.
- Select an augment and click an occupied building to permanently increase its level.
- Select Overclock and click **Use selected Overclock** to score the factory's current output.
- Play up to two cards per turn, or end a turn early. Factory output is added to the round total at turn end.
- Meet the quota before turns run out to enter the upgrade shop. Spend surplus production to upgrade, add, or remove deck cards; any unspent amount carries over.

The starter deck contains four production buildings, three enhancers, two augments, and one Overclock. The opening board is 3x3, the opening hand is four cards, the opening quota is six production, and the opening round has three turns. Production buildings start at one production per turn; each adjacent enhancer increases their output by 50% per level. These opening balance values are initial defaults for playtesting.
