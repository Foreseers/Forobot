package com.forobot.Bot;

import java.util.Locale;

import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;


/**
 * Synthesizes speech from text using FreeTTS libraries.
 * Can be toggled OFF and ON via spellMessages variable.
 * Bot(and other classes) are calling methods of this class to synthesize speech.
 */
public class SpeechSynthesizer {
    private SynthesizerModeDesc desc = new SynthesizerModeDesc(Locale.ENGLISH);
    private Synthesizer synthesizer;

    private boolean spellMessages = false;

    //TODO: More voices.
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
     * @param sender  The person who sent the message.
     * @param message The message that the person sent.
     */
    public void spell(String sender, String message) throws InterruptedException {
        if (spellMessages) {
            synchronized (synthesizer) {
                if (message.contains("http://") || message.contains("www.")) {
                    spell(sender);
                    spell(" sent a message with a link.");
                } else {
                    spell(sender + " said");
                    spell(message);
                }
            }
        }
    }

    /**
     * Spells the message.
     *
     * @param message Message to be spelled out.
     */
    private void spell(String message) throws InterruptedException {
        if (spellMessages) {
            if (message.length() > 60) {
                synthesizer.speakPlainText("a long message", null);
                synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
            } else {
                synthesizer.speakPlainText(message, null);
                synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
            }
        }
    }

    public void setSpellMessages(boolean spellMessages) {
        this.spellMessages = spellMessages;
    }

    public void close() {
        try {
            synthesizer.deallocate();
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }
}   
