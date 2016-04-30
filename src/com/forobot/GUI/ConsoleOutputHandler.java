package com.forobot.GUI;

import java.io.IOException;
import java.io.OutputStream;

import javafx.scene.control.TextArea;

/**
 * This class writes all messages that we get in System.out to text area.
 *
 * Only god knows why I have designed it to be this way, instead of just appending new text
 * to textArea directly...
 * Especially since System.out is only used by the bot itself to provide feedback to the user..
 *
 * It should actually be all rewritten, as nothing in the program directly writes to System.out, using LogHandler class instead.
 */
public class ConsoleOutputHandler extends OutputStream{

    private TextArea textArea;

    public ConsoleOutputHandler(TextArea textArea) {
        this.textArea = textArea;
    }

    /**
     * Asynchronously add new text to textArea via the main thread,
     * as all JAVAFX gui elements can not be edited outside the main thread.
     *
     * @param b     New text to be added, cast to char.
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException {
        javafx.application.Platform.runLater(() -> textArea.appendText(String.valueOf((char) b)));
    }
}
