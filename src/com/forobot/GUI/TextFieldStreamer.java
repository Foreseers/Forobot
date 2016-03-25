package com.forobot.GUI;

import java.awt.event.ActionEvent;
import java.io.InputStream;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Created by Foreseer on 16.03.2016.
 */
class TextFieldStreamer extends InputStream implements EventHandler<KeyEvent>, Runnable {

    private TextField tf;
    private String str = null;
    private int pos = 0;

    public TextFieldStreamer(TextField jtf) {
        tf = jtf;
    }

    @Override
    public void handle(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            str = tf.getText() + "\n";
            pos = 0;
            tf.setText("");
        }
        synchronized (this) {
            //maybe this should only notify() as multiple threads may
            //be waiting for input and they would now race for input
            this.notifyAll();
        }
    }

    @Override
    public int read() {
        //test if the available input has reached its end
        //and the EOS should be returned
        if (str != null && pos == str.length()) {
            str = null;
            //this is supposed to return -1 on "end of stream"
            //but I'm having a hard time locating the constant
            return java.io.StreamTokenizer.TT_EOF;
        }
        //no input available, block until more is available because that's
        //the behavior specified in the Javadocs
        while (str == null || pos >= str.length()) {
            try {
                //according to the docs read() should block until new input is available
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        //read an additional character, return it and increment the index
        return str.charAt(pos++);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){

        }
    }
}
