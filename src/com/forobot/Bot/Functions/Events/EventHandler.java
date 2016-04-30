package com.forobot.Bot.Functions.Events;

import com.forobot.Bot.Bot;
import com.forobot.Bot.Functions.Statistics;
import com.forobot.Utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles different events.
 */


public class EventHandler {

    //Variable containing reference to the current raid.
    private static Raid raid = null;
    //Variable containing reference to the current poll
    private static Poll poll = null;
    //Variable containing reference to the raid thread.
    private static Thread raidThread;
    //Variable containing reference to the poll thread.
    private static Thread pollThread;

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
    public static String initiateNewEvent(Bot bot, Statistics.Viewer viewer, int eventParam, EventType eventType) {
        switch (eventType) {
            case EVENT_ROULETTE: {
                if (eventParam == 0) {
                    if (roulette(viewer)) {
                        return String.format("Congratulations, %s! You now have %d coins!", viewer.getName(),
                                Statistics.getViewer(viewer.getName()).getMoneyAmount());
                    } else {
                        return String.format("You just lost all your coins, %s :(", eventParam, viewer.getName());
                    }
                }
                if (roulette(viewer, eventParam)) {
                    return String.format("Congratulations, %s! You now have %d coins!", viewer.getName(),
                            Statistics.getViewer(viewer.getName()).getMoneyAmount());
                } else {
                    return String.format("You just lost %d coins, %s :(", eventParam,  viewer.getName());
                }
            }
            case EVENT_RAID: {
                if (raid == null || raid.isFinished()) {
                    startNewRaid(bot, 50, 40);
                    raid.addParticipant(viewer, eventParam);
                } else {
                    raid.addParticipant(viewer, eventParam);
                }
                break;
            }
            case EVENT_POLL: {
                poll.addVote(viewer, eventParam);
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
        raidThread = new Thread(raid);
        raidThread.start();
        bot.sendMessage("New raid has started! Will be finished in " + duration + " seconds. " + percentage + "% participants will win.");
    }

    public static void startNewPoll(Bot bot, List<String> options, String question, int duration){
        poll = new Poll(EventType.EVENT_POLL, duration, bot, question, options);
        pollThread = new Thread(poll);
        pollThread.start();
        StringBuilder optionString = new StringBuilder();
        for (String option : options){
            optionString.append(String.format("%d-%s, ", options.indexOf(option), option));
        }
        optionString = new StringBuilder(optionString.substring(0, optionString.length() - 2));
        bot.sendMessage("New poll has started! It will be over in " + duration + "seconds." + " To vote, enter !vote option and send it to the chat. Options for voting are: " + optionString);
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

    private static void raid(Statistics.Viewer viewer, int amount) {

    }

    public enum EventType {
        EVENT_ROULETTE,
        EVENT_RAID,
        EVENT_POLL;
    }
}
