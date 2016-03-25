package com.forobot.GUI;

/**
 * Created by Foreseer on 16.03.2016.
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIApp.fxml"));
        TabPane pane = loader.load();
        primaryStage.setTitle("FooBot");
        primaryStage.setScene(new Scene(pane));
        primaryStage.setMaxHeight(528);
        primaryStage.setMaxWidth(839);
        primaryStage.setResizable(false);
        primaryStage.show();

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
