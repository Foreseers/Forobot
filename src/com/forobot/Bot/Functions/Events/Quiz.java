package com.forobot.Bot.Functions.Events;

import com.forobot.Bot.Bot;
import com.forobot.Bot.Functions.Statistics;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Konstantin on 29.04.2016.
 */
public class Quiz extends AbstractEvent{

    private String question;
    private String answer;

    private Map<String, String> guesses;
    private boolean gotRightAnswer;
    private Pair<Statistics.Viewer, String> rightAnswer;

    public Quiz(EventHandler.EventType type, int duration, Bot bot, String question, String answer) {
        super(type, duration, bot);
        this.question = question;
        this.answer = answer;

        guesses = Collections.synchronizedMap(new HashMap<>());
        gotRightAnswer = false;
    }

    @Override
    protected void eventAction() {
        while (!gotRightAnswer){
            if ((clock % 10 == 0) && (duration - clock != 0)){
                bot.sendMessage(String.format("It's %d seconds until the end of quiz! The question is - %s?", duration - clock, question));
            }
        }
    }

    @Override
    protected void eventFinish() {
        if (!gotRightAnswer) {
            Pair<String, String> bestGuess = findBestGuess();
            if (bestGuess == null) {
                bot.sendMessage("There were no good guesses among participants.");
                return;
            }
            bot.sendMessage(String.format("%s had the best guess: \"%s\"", bestGuess.getKey(), bestGuess.getValue()));
        }
        bot.sendMessage(String.format("%s answered correct: \"%s\"!", rightAnswer.getKey().getName(), rightAnswer.getValue()));
        return;
    }

    private Pair<String, String> findBestGuess(){
        Map<String, String> guessesContainingAnswer = new HashMap<>();
        for (Map.Entry<String, String> entry : guesses.entrySet()){
            if (entry.getValue().contains(answer)){
                guessesContainingAnswer.put(entry.getKey(), entry.getValue());
            }
        }

        if (guessesContainingAnswer.size() == 0){
            return null;
        }

        Pair<String, String> bestGuess = null;
        for (Map.Entry<String, String> entry : guessesContainingAnswer.entrySet()){
            if (bestGuess == null){
                bestGuess = new Pair<>(entry.getKey(), entry.getValue());
            } else {
                if (entry.getValue().length() < bestGuess.getValue().length()){
                    bestGuess = new Pair<>(entry.getKey(), entry.getValue());
                }
            }
        }

        return bestGuess;
    }

    public void participate(Statistics.Viewer viewer, String guess){
        if (guess.equals(answer)){
            rightAnswer = new Pair<>(viewer, guess);
            gotRightAnswer = true;
            return;
        }
        if (guesses.containsKey(viewer.getName())){
            return;
        }
        guesses.put(viewer.getName(), guess);
    }
}
