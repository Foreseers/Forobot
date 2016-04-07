package com.forobot.Bot.Functions.Events;

import com.forobot.Bot.Bot;
import com.forobot.Bot.Functions.Statistics;
import com.forobot.Utils.MiscUtils;

/**
 * Handles different events.
 */


public class EventHandler {

    private static Raid raid = null;
    private static Thread raidThread;

    public static String initiateNewEvent(Bot bot, Statistics.Viewer viewer, int amount, Event event) {
        switch (event) {
            case EVENT_ROULETTE: {
                if (amount == 0) {
                    if (roulette(viewer)) {
                        return String.format("Congratulations, %s! You now have %d coins!", viewer.getName(),
                                Statistics.getViewer(viewer.getName()).getMoneyAmount());
                    } else {
                        return String.format("You just lost all your coins, %s :(", amount, viewer.getName());
                    }
                }
                if (roulette(viewer, amount)) {
                    return String.format("Congratulations, %s! You now have %d coins!", viewer.getName(),
                            Statistics.getViewer(viewer.getName()).getMoneyAmount());
                } else {
                    return String.format("You just lost %d coins, %s :(", amount,  viewer.getName());
                }
            }
            case EVENT_RAID: {
                if (raid == null || raid.isFinished()) {
                    raid = new Raid(bot, 50, 40);
                    raidThread = new Thread(raid);
                    raidThread.start();
                    raid.addParticipant(viewer, amount);
                } else {
                    raid.addParticipant(viewer, amount);
                }
            }
        }
        return null;
    }

    public static boolean isThereAnActiveRaid() {
        if (raid == null || raid.isFinished()) {
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

    public enum Event {
        EVENT_ROULETTE,
        EVENT_RAID;
    }
}
