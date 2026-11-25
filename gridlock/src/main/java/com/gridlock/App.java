package com.gridlock;

import com.gridlock.controller.GameController;
import com.gridlock.model.GameState;
import com.gridlock.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class App extends Application {
    private GameController controller;

    @Override
    public void start(Stage stage) {
        GameState gameState = new GameState();
        MainView mainView = new MainView();
        controller = new GameController(gameState, mainView);

        stage.setTitle("Gridlock");
        stage.setMinWidth(850);
        stage.setMinHeight(650);
        stage.setScene(new Scene(mainView, 1000, 720));
        stage.show();
        controller.refresh();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
