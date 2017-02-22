package com.forobot.GUI;

/**
 * Don't touch.
 */

import com.forobot.Bot.Functions.Statistics;
import com.forobot.Bot.Handlers.LogHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

        //Get a controller object so we can shutdown the executor service
        guiController = loader.getController();
    }

    @Override
    public void stop() {
        Statistics.saveViewersListIntoAFile(guiController.getOperator().getVIEWERS_FILEPATH());
        LogHandler.close();
        guiController.getExecutorService().shutdownNow();
        try {
            if (!guiController.getExecutorService().awaitTermination(5, TimeUnit.SECONDS)){
                LogHandler.log("Timeouted, quitting normally.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Platform.exit();
        System.exit(0);
    }
}
