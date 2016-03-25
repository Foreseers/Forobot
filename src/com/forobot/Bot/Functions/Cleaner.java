package com.forobot.Bot.Functions;

import com.forobot.GUI.GUIController;

import javafx.scene.control.TextArea;

/**
 * This class is cleaning fields if they're containing too much information.
 * Though it's not really useful since newer versions of the bot as debug doesn't contain all the messages
 */
public class Cleaner implements Runnable {

    private TextArea textArea;
    private GUIController controller;

    public Cleaner(TextArea textArea, GUIController controller) {
        this.textArea = textArea;
        this.controller = controller;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (textArea.getText().length() > 5000) {
                controller.clearDebugArea();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
