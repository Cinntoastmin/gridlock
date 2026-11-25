package com.gridlock.controller;

import com.gridlock.model.Card;
import com.gridlock.model.CardType;
import com.gridlock.model.Difficulty;
import com.gridlock.model.GameState;
import com.gridlock.view.MainView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GameController {
    private final GameState gameState;
    private final MainView mainView;
    private final List<UUID> mulliganSelection = new ArrayList<>();
    private UUID selectedCardId;

    public GameController(GameState gameState, MainView mainView) {
        this.gameState = gameState;
        this.mainView = mainView;
        mainView.setActions(new MainView.Actions() {
            @Override
            public void selectCard(UUID cardId) {
                selectHandCard(cardId);
            }

            @Override
            public void selectMulliganCard(UUID cardId) {
                toggleMulliganCard(cardId);
            }

            @Override
            public void placeCard(int row, int column) {
                playOnSlot(row, column);
            }

            @Override
            public void playOverclock() {
                useOverclock();
            }

            @Override
            public void finishMulligan() {
                startRound();
            }

            @Override
            public void endTurn() {
                gameState.endTurn();
                refresh();
            }

            @Override
            public void setDifficulty(Difficulty difficulty) {
                gameState.setDifficulty(difficulty);
                refresh();
            }

            @Override
            public void upgradeCard(UUID cardId) {
                gameState.upgradeCard(cardId);
                refresh();
            }

            @Override
            public void addCard() {
                gameState.addProductionCard();
                refresh();
            }

            @Override
            public void removeCard(UUID cardId) {
                gameState.removeCard(cardId);
                refresh();
            }

            @Override
            public void finishShop() {
                gameState.finishShop();
                mulliganSelection.clear();
                selectedCardId = null;
                refresh();
            }
        });
    }

    public void refresh() {
        mainView.refresh(gameState, selectedCardId, mulliganSelection);
    }

    public void stop() {
    }

    private void selectHandCard(UUID cardId) {
        selectedCardId = cardId;
        refresh();
    }

    private void toggleMulliganCard(UUID cardId) {
        if (mulliganSelection.contains(cardId)) {
            mulliganSelection.remove(cardId);
        } else {
            mulliganSelection.add(cardId);
        }
        refresh();
    }

    private void playOnSlot(int row, int column) {
        if (selectedCardId == null) {
            return;
        }
        Card selected = null;
        for (Card card : gameState.getHand()) {
            if (card.getId().equals(selectedCardId)) {
                selected = card;
                break;
            }
        }
        if (selected == null) {
            selectedCardId = null;
        } else if (selected.getType() == CardType.AUGMENT) {
            if (gameState.augmentCard(selectedCardId, row, column)) {
                selectedCardId = null;
            }
        } else {
            if (gameState.placeCard(selectedCardId, row, column)) {
                selectedCardId = null;
            }
        }
        refresh();
    }

    private void useOverclock() {
        if (selectedCardId != null) {
            if (gameState.playOverclock(selectedCardId)) {
                selectedCardId = null;
            }
        }
        refresh();
    }

    private void startRound() {
        if (gameState.finishMulligan(new ArrayList<>(mulliganSelection))) {
            mulliganSelection.clear();
            selectedCardId = null;
        }
        refresh();
    }
}
