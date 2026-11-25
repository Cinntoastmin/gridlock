package com.gridlock.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class GameState {
    public static final int BOARD_ROWS = 3;
    public static final int BOARD_COLUMNS = 3;
    public static final int HAND_SIZE = 4;
    public static final int CARDS_PER_TURN = 2;
    public static final int INITIAL_TURNS = 3;
    public static final double INITIAL_QUOTA = 6;
    public static final double UPGRADE_COST = 8;
    public static final double ADD_CARD_COST = 12;
    public static final double REMOVE_CARD_COST = 6;

    private final Random random;
    private final List<Card> drawPile = new ArrayList<>();
    private final List<Card> discardPile = new ArrayList<>();
    private final List<Card> hand = new ArrayList<>();
    private final BoardGrid board = new BoardGrid(BOARD_ROWS, BOARD_COLUMNS);
    private GamePhase phase = GamePhase.MULLIGAN;
    private Difficulty difficulty = Difficulty.EASY;
    private int round = 1;
    private int turn = 1;
    private int turnsTaken;
    private int turnsThisRound = INITIAL_TURNS;
    private int cardsPlayedThisTurn;
    private double quota = INITIAL_QUOTA;
    private double roundProduction;
    private double shopProduction;
    private String status = "Choose any cards to mulligan, then start the round.";

    public GameState() {
        this(new Random());
    }

    public GameState(Random random) {
        this.random = random;
        createStartingDeck();
        Collections.shuffle(drawPile, random);
        drawToHandSize();
    }

    public GamePhase getPhase() {
        return phase;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getRound() {
        return round;
    }

    public int getTurn() {
        return turn;
    }

    public int getTurnsThisRound() {
        return turnsThisRound;
    }

    public int getCardsPlayedThisTurn() {
        return cardsPlayedThisTurn;
    }

    public double getQuota() {
        return quota;
    }

    public double getRoundProduction() {
        return roundProduction;
    }

    public double getShopProduction() {
        return shopProduction;
    }

    public String getStatus() {
        return status;
    }

    public BoardGrid getBoard() {
        return board;
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(new ArrayList<>(hand));
    }

    public List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>(drawPile);
        cards.addAll(discardPile);
        cards.addAll(hand);
        for (BoardSlot slot : board.getSlots()) {
            if (slot.getCard() != null) {
                cards.add(slot.getCard());
            }
        }
        return Collections.unmodifiableList(cards);
    }

    public double getFactoryProduction() {
        double production = 0;
        for (BoardSlot slot : board.getSlots()) {
            Card card = slot.getCard();
            if (card != null && card.getType() == CardType.PRODUCTION) {
                EffectContext context = new EffectContext(this, slot, board.getAdjacentSlots(slot));
                production += card.productionPerSecond(context);
            }
        }
        return production;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (phase != GamePhase.MULLIGAN || round != 1 || turnsTaken != 0) {
            status = "Difficulty can only be changed before the first turn.";
            return;
        }
        this.difficulty = difficulty;
        status = "Difficulty set to " + difficulty.getDisplayName() + ". Choose cards to mulligan.";
    }

    public boolean finishMulligan(List<UUID> cardIds) {
        if (phase != GamePhase.MULLIGAN) {
            status = "You can only mulligan at the start of a round.";
            return false;
        }
        List<Card> selected = new ArrayList<>();
        for (UUID id : cardIds) {
            Card card = findInHand(id);
            if (card == null || containsCard(selected, id)) {
                status = "The selected mulligan cards are no longer in your hand.";
                return false;
            }
            selected.add(card);
        }

        hand.removeAll(selected);
        drawToHandSize();
        drawPile.addAll(selected);
        Collections.shuffle(drawPile, random);
        phase = GamePhase.PLAYING;
        status = "Round " + round + " started. Play up to two cards this turn.";
        return true;
    }

    public boolean placeCard(UUID cardId, int row, int column) {
        if (!canPlayCard()) {
            return false;
        }
        Card card = findInHand(cardId);
        if (card == null || (card.getType() != CardType.PRODUCTION && card.getType() != CardType.ENHANCER)) {
            status = "Select a production or enhancement building to place.";
            return false;
        }
        if (!board.placeCard(card, row, column)) {
            status = "That slot is occupied.";
            return false;
        }
        hand.remove(card);
        cardsPlayedThisTurn++;
        status = card.getName() + " placed.";
        finishTurnAfterLastPlay();
        return true;
    }

    public boolean augmentCard(UUID cardId, int row, int column) {
        if (!canPlayCard()) {
            return false;
        }
        Card augment = findInHand(cardId);
        if (augment == null || augment.getType() != CardType.AUGMENT) {
            status = "Select an augment card first.";
            return false;
        }
        BoardSlot target = board.getSlot(row, column);
        if (!target.isOccupied()) {
            status = "Place an augment on an occupied building.";
            return false;
        }
        target.getCard().upgrade();
        hand.remove(augment);
        discardPile.add(augment);
        cardsPlayedThisTurn++;
        status = target.getCard().getName() + " upgraded to level " + target.getCard().getLevel() + ".";
        finishTurnAfterLastPlay();
        return true;
    }

    public boolean playOverclock(UUID cardId) {
        if (!canPlayCard()) {
            return false;
        }
        Card card = findInHand(cardId);
        if (card == null || card.getType() != CardType.OVERCLOCK) {
            status = "Select an Overclock card first.";
            return false;
        }
        double gain = getFactoryProduction();
        roundProduction += gain;
        hand.remove(card);
        discardPile.add(card);
        cardsPlayedThisTurn++;
        status = "Overclock added " + format(gain) + " production.";
        finishTurnAfterLastPlay();
        return true;
    }

    public void endTurn() {
        if (phase != GamePhase.PLAYING) {
            status = "There is no active turn to end.";
            return;
        }
        finishTurn();
    }

    public boolean upgradeCard(UUID cardId) {
        if (!canShop()) {
            return false;
        }
        Card card = findOwnedCard(cardId);
        if (card == null || (card.getType() != CardType.PRODUCTION && card.getType() != CardType.ENHANCER)) {
            status = "Choose a building card to upgrade.";
            return false;
        }
        if (shopProduction < UPGRADE_COST) {
            status = "Not enough unspent production to upgrade a card.";
            return false;
        }
        card.upgrade();
        shopProduction -= UPGRADE_COST;
        status = card.getName() + " upgraded to level " + card.getLevel() + ".";
        return true;
    }

    public boolean addProductionCard() {
        if (!canShop()) {
            return false;
        }
        if (shopProduction < ADD_CARD_COST) {
            status = "Not enough unspent production to add a card.";
            return false;
        }
        discardPile.add(new Card(CardType.PRODUCTION));
        shopProduction -= ADD_CARD_COST;
        status = "Added a production building to your deck.";
        return true;
    }

    public boolean removeCard(UUID cardId) {
        if (!canShop()) {
            return false;
        }
        Card card = findOwnedCard(cardId);
        if (card == null) {
            status = "Choose a card in your deck to remove.";
            return false;
        }
        if (shopProduction < REMOVE_CARD_COST) {
            status = "Not enough unspent production to remove a card.";
            return false;
        }
        removeFromPiles(card);
        shopProduction -= REMOVE_CARD_COST;
        status = card.getName() + " removed from your deck.";
        return true;
    }

    public boolean finishShop() {
        if (!canShop()) {
            return false;
        }
        round++;
        quota = Math.ceil(quota * difficulty.getQuotaMultiplier());
        turnsThisRound = difficulty.getNextRoundTurns();
        turnsTaken = 0;
        turn = 1;
        cardsPlayedThisTurn = 0;
        roundProduction = shopProduction;
        shopProduction = 0;
        drawToHandSize();
        phase = GamePhase.MULLIGAN;
        status = "Round " + round + ": mulligan any cards before playing.";
        return true;
    }

    private void createStartingDeck() {
        addCards(CardType.PRODUCTION, 4);
        addCards(CardType.ENHANCER, 3);
        addCards(CardType.AUGMENT, 2);
        addCards(CardType.OVERCLOCK, 1);
    }

    private void addCards(CardType type, int count) {
        for (int i = 0; i < count; i++) {
            drawPile.add(new Card(type));
        }
    }

    private void drawToHandSize() {
        while (hand.size() < HAND_SIZE) {
            if (drawPile.isEmpty()) {
                if (discardPile.isEmpty()) {
                    break;
                }
                drawPile.addAll(discardPile);
                discardPile.clear();
                Collections.shuffle(drawPile, random);
            }
            hand.add(drawPile.remove(drawPile.size() - 1));
        }
    }

    private boolean canPlayCard() {
        if (phase != GamePhase.PLAYING) {
            status = "Finish the mulligan before playing cards.";
            return false;
        }
        if (cardsPlayedThisTurn >= CARDS_PER_TURN) {
            status = "You have already played two cards this turn.";
            return false;
        }
        return true;
    }

    private void finishTurnAfterLastPlay() {
        if (cardsPlayedThisTurn >= CARDS_PER_TURN) {
            finishTurn();
        }
    }

    private void finishTurn() {
        roundProduction += getFactoryProduction();
        turnsTaken++;
        cardsPlayedThisTurn = 0;

        if (roundProduction >= quota) {
            shopProduction = roundProduction - quota;
            prepareShop();
            phase = GamePhase.SHOP;
            status = "Quota met! Spend " + format(shopProduction) + " production or carry it forward.";
        } else if (turnsTaken >= turnsThisRound) {
            phase = GamePhase.GAME_OVER;
            status = "Quota missed. The factory produced " + format(roundProduction)
                    + " of " + format(quota) + " production.";
        } else {
            turn++;
            drawToHandSize();
            status = "Turn " + turn + " of " + turnsThisRound + ". Play up to two cards.";
        }
    }

    private void prepareShop() {
        discardPile.addAll(hand);
        hand.clear();
        discardPile.addAll(board.clear());
        Collections.shuffle(discardPile, random);
    }

    private boolean canShop() {
        if (phase != GamePhase.SHOP) {
            status = "The shop is only available after meeting the quota.";
            return false;
        }
        return true;
    }

    private Card findInHand(UUID id) {
        for (Card card : hand) {
            if (card.getId().equals(id)) {
                return card;
            }
        }
        return null;
    }

    private Card findOwnedCard(UUID id) {
        for (Card card : getAllCards()) {
            if (card.getId().equals(id)) {
                return card;
            }
        }
        return null;
    }

    private boolean containsCard(List<Card> cards, UUID id) {
        for (Card card : cards) {
            if (card.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void removeFromPiles(Card card) {
        drawPile.remove(card);
        discardPile.remove(card);
        hand.remove(card);
    }

    private String format(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : String.format("%.1f", value);
    }
}
