package com.forobot.GUI;

/**
 * Application is the main class of any JavaFX applications, and so is of mine.
 * Don't touch.
 * If you touch something besides final variables, the program will refuse to work properly.
 * Variables are very sensitive to touching as well and don't like being messed with, either.
 */

import com.forobot.Bot.Functions.Statistics;
import com.forobot.Bot.Handlers.LogHandler;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class GUIApp extends Application {

    private GUIController guiController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        final int height = 528;
        final int width = 839;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIApp.fxml"));
        TabPane pane = loader.load();
        primaryStage.setTitle("FooBot");
        primaryStage.setScene(new Scene(pane));
        primaryStage.setMaxHeight(height);
        primaryStage.setMaxWidth(width);
        primaryStage.setResizable(false);
        primaryStage.show();

        /*  Could as well be commented out, does nothing, i'll leave it be, however, to allow
         *  direct manipulations with the controller when needed.
         *  Doubt it'll ever be needed, though.
         */
        guiController = loader.getController();
    }

    @Override
    public void stop() {
        Statistics.saveViewersListIntoAFile(guiController.getOperator().getVIEWERS_FILEPATH());
        LogHandler.close();
        Platform.exit();
        System.exit(0);
    }
}
