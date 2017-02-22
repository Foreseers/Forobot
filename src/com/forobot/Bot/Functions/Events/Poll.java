package com.forobot.Bot.Functions.Events;

import com.forobot.Bot.Bot;
import com.forobot.Bot.Functions.Statistics;
import com.forobot.Utils.MiscUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Konstantin on 29.04.2016.
 */
public class Poll extends AbstractEvent {

    private Map<String, Integer> answersMap;
    private List<String> answersList;
    private String question;

    private List<String> participantList;

    public Poll(EventHandler.EventType type, int duration, Bot bot, String question, List<String> answers) {
        super(type, duration, bot);
        this.answersMap = Collections.synchronizedMap(new HashMap<>());
        for (String answer : answers) {
            this.answersMap.put(answer, 0);
        }
        this.answersList = answers;
        this.question = question;
        this.participantList = Collections.synchronizedList(new ArrayList<>());

    }

    @Override
    protected void eventAction() {
        if ((clock % 10 == 0) && (duration - clock != 0)) {
            StringBuilder infoString = new StringBuilder(String.format("It's %s seconds until poll finishes!", duration - clock));
            infoString.append(" You can vote for following options: ");
            for (String option : answersList) {
                int index = answersList.indexOf(option);
                infoString.append(String.format("%d for option \"%s\", ", index, option));
            }
            infoString = new StringBuilder(infoString.substring(0, infoString.length() - 2));
            bot.sendMessage(infoString.toString());
        }

    }

    @Override
    protected void eventFinish() {
        int maxCount = 0;
        String maxOption = "";
        for (Map.Entry<String, Integer> entry : answersMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                if (maxOption.equals("")) {
                    maxCount = entry.getValue();
                    maxOption = entry.getKey();
                } else {
                    maxCount = entry.getValue();
                    maxOption = entry.getKey();
                }
            }
            if (entry.getValue() == maxCount){
                int rnd = MiscUtils.randomWithRange(0, 1);
                if (rnd == 0){
                    maxOption = entry.getKey();
                    maxCount = entry.getValue();
                }
            }
        }
        bot.sendMessage(String.format("Option \"%s\" got most votes - %d!", maxOption, maxCount));
    }

    public void addVote(Statistics.Viewer viewer, int voteIndex) {
        if (participantList.contains(viewer.getName())) {
            return;
        }
        if (voteIndex > answersList.size() - 1 || voteIndex < 0) {
            return;
        }
        participantList.add(viewer.getName());
        String chosenOption = answersList.get(voteIndex);
        int count = answersMap.get(chosenOption);
        answersMap.put(chosenOption, ++count);
    }

    public String getQuestion() {
        return question;
    }

    public int amountOfOptions() {
        return answersList.size();
    }
}
