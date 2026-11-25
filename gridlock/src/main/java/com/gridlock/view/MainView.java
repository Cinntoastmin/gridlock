package com.gridlock.view;

import com.gridlock.model.BoardSlot;
import com.gridlock.model.Card;
import com.gridlock.model.CardType;
import com.gridlock.model.Difficulty;
import com.gridlock.model.GamePhase;
import com.gridlock.model.GameState;
import java.util.List;
import java.util.UUID;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class MainView extends BorderPane {
    public interface Actions {
        void selectCard(UUID cardId);
        void selectMulliganCard(UUID cardId);
        void placeCard(int row, int column);
        void playOverclock();
        void finishMulligan();
        void endTurn();
        void setDifficulty(Difficulty difficulty);
        void upgradeCard(UUID cardId);
        void addCard();
        void removeCard(UUID cardId);
        void finishShop();
    }

    private final Label roundLabel = new Label();
    private final Label quotaLabel = new Label();
    private final Label productionLabel = new Label();
    private final Label factoryLabel = new Label();
    private final Label turnLabel = new Label();
    private final Label statusLabel = new Label();
    private final GridPane boardView = new GridPane();
    private final HBox handView = new HBox(10);
    private Actions actions;

    public MainView() {
        setPadding(new Insets(18));
        setStyle("-fx-background-color: #111827; -fx-font-family: 'Segoe UI';");
        buildLayout();
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    public void refresh(GameState state, UUID selectedCardId, List<UUID> mulliganSelection) {
        roundLabel.setText("ROUND " + state.getRound());
        quotaLabel.setText("Quota: " + format(state.getQuota()));
        productionLabel.setText("Round production: " + format(state.getRoundProduction()));
        factoryLabel.setText("Factory / turn: " + format(state.getFactoryProduction()));
        turnLabel.setText("Turn " + state.getTurn() + " / " + state.getTurnsThisRound()
                + "     Cards " + state.getCardsPlayedThisTurn() + " / " + GameState.CARDS_PER_TURN);
        statusLabel.setText(state.getStatus());

        refreshBoard(state, selectedCardId);
        refreshHand(state, selectedCardId, mulliganSelection);
        updatePhaseControls(state, selectedCardId);
    }

    private void buildLayout() {
        Label title = new Label("GRIDLOCK");
        title.setStyle("-fx-text-fill: #f9fafb; -fx-font-size: 24px; -fx-font-weight: bold;");
        roundLabel.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox metrics = new HBox(28, roundLabel, quotaLabel, productionLabel, factoryLabel);
        styleMetric(quotaLabel);
        styleMetric(productionLabel);
        styleMetric(factoryLabel);
        VBox header = new VBox(12, title, metrics, turnLabel);
        turnLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
        header.setPadding(new Insets(0, 0, 18, 0));
        setTop(header);

        boardView.setHgap(8);
        boardView.setVgap(8);
        boardView.setAlignment(Pos.CENTER);
        setCenter(boardView);

        statusLabel.setWrapText(true);
        statusLabel.setMinHeight(44);
        statusLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
        VBox footer = new VBox(12, statusLabel, handView);
        footer.setPadding(new Insets(18, 0, 0, 0));
        setBottom(footer);
    }

    private void refreshBoard(GameState state, UUID selectedCardId) {
        boardView.getChildren().clear();
        for (int row = 0; row < GameState.BOARD_ROWS; row++) {
            for (int column = 0; column < GameState.BOARD_COLUMNS; column++) {
                BoardSlot slot = state.getBoard().getSlot(row, column);
                Button tile = new Button();
                tile.setPrefSize(172, 132);
                tile.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                if (slot.getCard() == null) {
                    tile.setText("EMPTY");
                    tile.setStyle(boardStyle(false));
                } else {
                    Card card = slot.getCard();
                    tile.setText(card.getName() + "\nLv. " + card.getLevel()
                            + (card.getType() == CardType.PRODUCTION
                            ? "\n" + format(card.getBaseProduction()) + " base / turn" : ""));
                    tile.setStyle(boardStyle(true));
                }
                int targetRow = row;
                int targetColumn = column;
                tile.setOnAction(event -> actions.placeCard(targetRow, targetColumn));
                boardView.add(tile, column, row);
            }
        }
    }

    private void refreshHand(GameState state, UUID selectedCardId, List<UUID> mulliganSelection) {
        handView.getChildren().clear();
        for (Card card : state.getHand()) {
            Button cardButton = new Button(card.getName() + "\nLv. " + card.getLevel());
            cardButton.setPrefSize(158, 74);
            cardButton.setMaxWidth(Double.MAX_VALUE);
            boolean selected = card.getId().equals(selectedCardId);
            boolean mulliganed = mulliganSelection.contains(card.getId());
            cardButton.setStyle(cardStyle(selected, mulliganed));
            cardButton.setOnAction(event -> {
                if (state.getPhase() == GamePhase.MULLIGAN) {
                    actions.selectMulliganCard(card.getId());
                } else {
                    actions.selectCard(card.getId());
                }
            });
            handView.getChildren().add(cardButton);
        }
    }

    private void updatePhaseControls(GameState state, UUID selectedCardId) {
        VBox controls = new VBox(12);
        controls.setPrefWidth(245);
        controls.setPadding(new Insets(14));
        controls.setStyle("-fx-background-color: #1f2937; -fx-background-radius: 12;");

        if (state.getPhase() == GamePhase.MULLIGAN) {
            Label help = helpText("Choose cards to replace. New cards are drawn before the selected cards are shuffled back.");
            ChoiceBox<Difficulty> difficultyChoice = new ChoiceBox<>();
            difficultyChoice.getItems().addAll(Difficulty.values());
            difficultyChoice.setValue(state.getDifficulty());
            difficultyChoice.setOnAction(event -> actions.setDifficulty(difficultyChoice.getValue()));
            difficultyChoice.setDisable(state.getRound() != 1 || state.getTurn() != 1);
            Button continueButton = primaryButton("Start turn");
            continueButton.setOnAction(event -> actions.finishMulligan());
            controls.getChildren().addAll(heading("MULLIGAN"), help, difficultyChoice, continueButton);
        } else if (state.getPhase() == GamePhase.PLAYING) {
            Button overclockButton = primaryButton("Use selected Overclock");
            overclockButton.setOnAction(event -> actions.playOverclock());
            overclockButton.setDisable(!isSelectedType(state, selectedCardId, CardType.OVERCLOCK));
            Button endTurnButton = primaryButton("End turn");
            endTurnButton.setOnAction(event -> actions.endTurn());
            controls.getChildren().addAll(
                    heading("YOUR FACTORY"),
                    helpText("Select a building, then click an empty slot. Select an augment and click a building to upgrade it."),
                    overclockButton,
                    endTurnButton);
        } else if (state.getPhase() == GamePhase.SHOP) {
            ListView<Card> cards = new ListView<>();
            cards.getItems().addAll(state.getAllCards());
            cards.setPrefHeight(180);
            Button upgrade = primaryButton("Upgrade selected building (8)");
            upgrade.setOnAction(event -> {
                Card card = cards.getSelectionModel().getSelectedItem();
                if (card != null) {
                    actions.upgradeCard(card.getId());
                }
            });
            Button remove = secondaryButton("Remove selected card (6)");
            remove.setOnAction(event -> {
                Card card = cards.getSelectionModel().getSelectedItem();
                if (card != null) {
                    actions.removeCard(card.getId());
                }
            });
            Button add = secondaryButton("Add production building (12)");
            add.setOnAction(event -> actions.addCard());
            Button nextRound = primaryButton("Begin next round");
            nextRound.setOnAction(event -> actions.finishShop());
            controls.getChildren().addAll(
                    heading("UPGRADE PHASE"),
                    helpText("Unspent production: " + format(state.getShopProduction())
                            + ". Any remainder carries into the next round."),
                    cards, upgrade, remove, add, nextRound);
        } else {
            controls.getChildren().addAll(
                    heading("GAME OVER"),
                    helpText(state.getStatus()),
                    helpText("Final production: " + format(state.getRoundProduction())
                            + " / " + format(state.getQuota())));
        }
        setRight(controls);
    }

    private boolean isSelectedType(GameState state, UUID selected, CardType type) {
        if (selected == null) {
            return false;
        }
        for (Card card : state.getHand()) {
            if (card.getId().equals(selected)) {
                return card.getType() == type;
            }
        }
        return false;
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #f9fafb; -fx-font-weight: bold; -fx-font-size: 15px;");
        return label;
    }

    private Label helpText(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        return label;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold;");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: #374151; -fx-text-fill: #f9fafb;");
        return button;
    }

    private void styleMetric(Label label) {
        label.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 13px;");
    }

    private String boardStyle(boolean occupied) {
        return occupied
                ? "-fx-background-color: #3730a3; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-background-radius: 10;"
                : "-fx-background-color: #1f2937; -fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-border-color: #475569; -fx-border-radius: 10; -fx-background-radius: 10;";
    }

    private String cardStyle(boolean selected, boolean mulliganed) {
        String color = mulliganed ? "#7f1d1d" : selected ? "#312e81" : "#1f2937";
        return "-fx-background-color: " + color
                + "; -fx-text-fill: #f9fafb; -fx-font-size: 12px; -fx-background-radius: 10;";
    }

    private String format(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : String.format("%.1f", value);
    }
}
