package com.forobot.GUI;

import java.io.IOException;
import java.io.OutputStream;

import javafx.scene.control.TextArea;

/**
 * Created by Foreseer on 16.03.2016.
 */
public class ConsoleOutputHandler extends OutputStream{

    private TextArea textArea;

    public ConsoleOutputHandler(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        javafx.application.Platform.runLater(() -> textArea.appendText(String.valueOf((char) b)));
    }
}
