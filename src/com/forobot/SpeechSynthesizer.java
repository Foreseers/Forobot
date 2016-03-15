package com.forobot;

import java.util.Locale;

import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;


/**
 * Synthesizes speech from text.
 */
public class SpeechSynthesizer {
    private SynthesizerModeDesc desc = new SynthesizerModeDesc(Locale.ENGLISH);
    private Synthesizer synthesizer;

    public SpeechSynthesizer() {
        System.setProperty("freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        try {
            Central.registerEngineCentral
                    ("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            synthesizer = Central.createSynthesizer(desc);
            synthesizer.allocate();
            synthesizer.resume();
        } catch (EngineException e) {
            e.printStackTrace();
        } catch (AudioException e) {
            e.printStackTrace();
        }
    }

    /**
     * Synthesizes speech following this format: "SENDER said MESSAGE".
     *
     * @param sender  The person who sent the message
     * @param message The message that the person sent
     */
    public void spell(String sender, String message) throws InterruptedException {

        if (message.contains("http://") || message.contains("www.")) {
            spell(sender);
            spell(" sent a message with a link.");
        } else {
            spell(sender + " said");
            spell(message);
        }
    }

    /**
     * Spells the message.
     *
     * @param message that needs to be spelled.
     */
    private void spell(String message) throws InterruptedException {
        synthesizer.speakPlainText(message, null);
        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
    }

    public void close() {
        try {
            synthesizer.deallocate();
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }
}
