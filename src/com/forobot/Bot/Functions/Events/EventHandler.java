package com.forobot.Bot.Functions.Events;

import com.forobot.Bot.Bot;
import com.forobot.Bot.Functions.Statistics;
import com.forobot.Utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.control.TextField;

/**
 * Handles different events.
 */


public class EventHandler {

    //Variable containing reference to the current raid.
    private static Raid raid = null;
    //Variable containing reference to the current poll
    private static Poll poll = null;
    private static Quiz quiz = null;
    private static Raffle raffle = null;
    //Variable containing reference to the raid thread.
    private static Thread raidThread;
    //Variable containing reference to the poll thread.
    private static Thread pollThread;
    private static Thread quizThread;
    private static ExecutorService executorService;

    static {
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * Initiates or handles a request to the event.
     * If event is instant(roulette), processes the request and provides response to a user.
     * In case if event is non-instant, such as raid event, adds user as a participant of the event.
     *
     * @param bot       Bot that called this method.
     * @param viewer    Viewer that sent a request
     * @param eventParam    Amount of money that user participates with
     * @param eventType     Type of the event
     * @return          Response to a request. Actually should be fixed and sent directly via bot.
     */
    public static String initiateNewEvent(Bot bot, Statistics.Viewer viewer, String eventParam, EventType eventType) {
        switch (eventType) {
            case EVENT_ROULETTE: {
                if (Integer.parseInt(eventParam) == 0) {
                    if (roulette(viewer)) {
                        return String.format("Congratulations, %s! You now have %d coins!", viewer.getName(),
                                Statistics.getViewer(viewer.getName()).getMoneyAmount());
                    } else {
                        return String.format("You just lost all your coins, %s :(", viewer.getName());
                    }
                }
                if (roulette(viewer, Integer.parseInt(eventParam))) {
                    return String.format("Congratulations, %s! You now have %d coins!", viewer.getName(),
                            Statistics.getViewer(viewer.getName()).getMoneyAmount());
                } else {
                    return String.format("You just lost %d coins, %s :(", eventParam,  viewer.getName());
                }
            }
            case EVENT_RAID: {
                if (raid == null || raid.isFinished()) {
                    startNewRaid(bot, 50, 40);
                    raid.addParticipant(viewer, Integer.parseInt(eventParam));
                } else {
                    raid.addParticipant(viewer, Integer.parseInt(eventParam));
                }
                break;
            }
            case EVENT_POLL: {
                poll.addVote(viewer, Integer.parseInt(eventParam));
                break;
            }
            case EVENT_QUIZ: {
                quiz.participate(viewer, eventParam);
                break;
            }
            case EVENT_RAFFLE: {
                raffle.participate(viewer.getName());
                break;
            }
        }
        return null;
    }

    /**
     * Starts new raid with certain duration and percentage specified by the user.
     * Creates new Raid object and a thread and starts them, then send message to the channel that the raid has begun.
     * @param bot           Reference to a bot object, used to send messages to the channel.
     * @param percentage    Percentage of people that will will turn winners, out of all participants.
     * @param duration      Duration of the event, in seconds.
     */
    public static void startNewRaid(Bot bot, int percentage, int duration){
        raid = new Raid(bot, percentage, duration);
        executorService.execute(raid);
        bot.sendMessage("New raid has started! Will be finished in " + duration + " seconds. " + percentage + "% participants will win.");
    }

    public static void startNewPoll(Bot bot, List<String> options, String question, int duration){
        poll = new Poll(EventType.EVENT_POLL, duration, bot, question, options);
        executorService.execute(poll);
        StringBuilder optionString = new StringBuilder();
        for (String option : options){
            optionString.append(String.format("%d-%s, ", options.indexOf(option), option));
        }
        optionString = new StringBuilder(optionString.substring(0, optionString.length() - 2));
        bot.sendMessage("New poll has started! It will be over in " + duration + "seconds." + " To vote, enter !vote option and send it to the chat. Options for voting are: " + optionString);
    }

    public static void startNewQuiz(Bot bot, String question, String answer, int duration){
        quiz = new Quiz(EventType.EVENT_QUIZ, duration, bot, question, answer);
        executorService.execute(poll);
        StringBuilder info = new StringBuilder("New quiz has started! It will be over in ")
                            .append(duration)
                            .append(" seconds! The question is - ")
                            .append(question)
                            .append("?");
        bot.sendMessage(info.toString());
    }

    public static void startNewRaffle(Bot bot, int duration, TextField textField){
        raffle = new Raffle(EventType.EVENT_RAFFLE, duration, bot, textField);
        executorService.execute(raffle);
        StringBuilder info = new StringBuilder("New raffle has started! It will be over in ")
                                    .append(duration)
                                    .append(" seconds! To participate, send \"!raffle\" in the chat.");
        bot.sendMessage(info.toString());
    }

    public static boolean isThereAnActiveRaid() {
        if (raid == null || raid.isFinished()) {
            return false;
        }
        return true;
    }

    public static boolean isThereAnActivePoll() {
        if (poll == null || poll.isFinished()) {
            return false;
        }
        return true;
    }

    public static boolean isThereAnActiveQuiz() {
        if (quiz == null || quiz.isFinished()) {
            return false;
        }
        return true;
    }

    public static boolean isThereAnActiveRaffle() {
        if (raffle == null || raffle.isFinished()) {
            return false;
        }
        return true;
    }

    private static boolean roulette(Statistics.Viewer viewer) {
        int result = MiscUtils.randomWithRange(1, 10);
        if (result >= 1 && result <= 5) {
            viewer.setMoneyAmount(0);
            return false;
        } else {
            viewer.setMoneyAmount(viewer.getMoneyAmount() * 2);
            return true;
        }
    }

    private static boolean roulette(Statistics.Viewer viewer, int amount) {
        int result = MiscUtils.randomWithRange(1, 10);
        if (result >= 1 && result <= 5) {
            viewer.setMoneyAmount(viewer.getMoneyAmount() - amount);
            return false;
        } else {
            viewer.setMoneyAmount(viewer.getMoneyAmount() + amount);
            return true;
        }
    }

    public enum EventType {
        EVENT_ROULETTE,
        EVENT_RAID,
        EVENT_POLL,
        EVENT_QUIZ,
        EVENT_RAFFLE
    }
}
