package com.gridlock.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.Test;

public class GameStateTest {
    @Test
    public void startsWithTheSpecifiedDeckAndReplacesMulliganCardsBeforeReturningThem() {
        GameState state = new GameState(new Random(5));
        assertEquals(10, state.getAllCards().size());
        assertEquals(4, state.getHand().size());
        assertEquals(4, countCards(state, CardType.PRODUCTION));
        assertEquals(3, countCards(state, CardType.ENHANCER));
        assertEquals(2, countCards(state, CardType.AUGMENT));
        assertEquals(1, countCards(state, CardType.OVERCLOCK));

        List<UUID> mulligan = new ArrayList<>();
        for (Card card : state.getHand()) {
            mulligan.add(card.getId());
        }

        assertTrue(state.finishMulligan(mulligan));
        assertEquals(GamePhase.PLAYING, state.getPhase());
        assertEquals(4, state.getHand().size());
        assertEquals(10, state.getAllCards().size());
    }

    @Test
    public void missingQuotaAfterTheFinalTurnEndsTheGame() {
        GameState state = new GameState(new Random(5));
        assertTrue(state.finishMulligan(new ArrayList<UUID>()));

        state.endTurn();
        assertEquals(2, state.getTurn());
        state.endTurn();
        assertEquals(3, state.getTurn());
        state.endTurn();

        assertEquals(GamePhase.GAME_OVER, state.getPhase());
        assertEquals(0, state.getRoundProduction(), 0.0001);
    }

    @Test
    public void meetingQuotaOpensTheShopAndCarriesSurplusIntoTheNextRound() {
        GameState state = new GameState(new Random(5));
        assertTrue(state.finishMulligan(new ArrayList<UUID>()));
        assertTrue(state.getBoard().placeCard(new Card(CardType.PRODUCTION), 0, 0));
        assertTrue(state.getBoard().placeCard(new Card(CardType.PRODUCTION), 0, 1));
        assertTrue(state.getBoard().placeCard(new Card(CardType.PRODUCTION), 0, 2));
        assertTrue(state.getBoard().placeCard(new Card(CardType.PRODUCTION), 1, 0));
        assertTrue(state.getBoard().placeCard(new Card(CardType.ENHANCER), 1, 1));

        state.endTurn();
        assertEquals(GamePhase.PLAYING, state.getPhase());
        state.endTurn();

        assertEquals(GamePhase.SHOP, state.getPhase());
        assertEquals(4, state.getShopProduction(), 0.0001);
        assertTrue(state.finishShop());
        assertEquals(GamePhase.MULLIGAN, state.getPhase());
        assertEquals(2, state.getRound());
        assertEquals(9, state.getQuota(), 0.0001);
        assertEquals(4, state.getRoundProduction(), 0.0001);
    }

    private int countCards(GameState state, CardType type) {
        int count = 0;
        for (Card card : state.getAllCards()) {
            if (card.getType() == type) {
                count++;
            }
        }
        return count;
    }
}
