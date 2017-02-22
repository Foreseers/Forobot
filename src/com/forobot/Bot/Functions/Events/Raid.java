package com.forobot.Bot.Functions.Events;

import com.forobot.Bot.Bot;
import com.forobot.Bot.Functions.Statistics;
import com.forobot.Utils.MiscUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Foreseer on 06/04/2016.
 */
public class Raid extends AbstractEvent {
    private AtomicInteger coinAmount;
    private Map<Statistics.Viewer, Integer> raidInfo;
    private Map<Statistics.Viewer, Integer> raidResult;
    private int percentage;


    public Raid(Bot bot, int percentage, int duration) {
        super(EventHandler.EventType.EVENT_RAID, duration, bot);
        raidInfo = Collections.synchronizedMap(new HashMap<>());
        raidResult = Collections.synchronizedMap(new HashMap<>());
        coinAmount = new AtomicInteger();
        this.percentage = percentage;
    }

    public int getCoinAmount() {
        return coinAmount.get();
    }

    public void setCoinAmount(int coinAmount) {
        this.coinAmount.set(coinAmount);
    }

    public void addParticipant(Statistics.Viewer viewer, int amount) {
        if (!raidInfo.containsKey(viewer)) {
            coinAmount.addAndGet(amount);
            raidInfo.put(viewer, amount);
        } else {
            coinAmount.addAndGet(amount);
            raidInfo.put(viewer, raidInfo.get(viewer) + amount);
        }
        bot.sendMessage("Handled your request for participation in the raid with " + amount + " coins, " + viewer.getName());
    }

    @Override
    protected void eventAction() {
        if ((clock % 10 == 0) && (duration - clock != 0)) {
            bot.sendMessage(String.format("It's %d seconds until the raid finishes!", duration - clock));
        }
    }

    @Override
    protected void eventFinish() {
        double totalAmount = raidInfo.size();
        int neededAmount = (int) ((totalAmount / 100) * percentage);
        if (neededAmount < 1) {
            neededAmount = 1;
        }
        for (int i = 1; i <= neededAmount; i++) {
            int r = MiscUtils.randomWithRange(0, raidInfo.size() - 1);
            int j = 0;
            int coinsAmount = 0;
            Statistics.Viewer viewer = null;
            for (Iterator<Map.Entry<Statistics.Viewer, Integer>> iterator = raidInfo.entrySet().iterator(); j <= r; ) {
                if (j == r) {
                    Map.Entry<Statistics.Viewer, Integer> entry = iterator.next();
                    viewer = entry.getKey();
                    coinsAmount = entry.getValue();
                    iterator.remove();
                    break;
                }
                iterator.next();
                j++;
            }
            raidResult.put(viewer, coinsAmount);
            Statistics.increaseCoinsAmount(viewer.getName(), coinsAmount * 2);
            //bot.sendMessage("Congratulation, " + viewer.getName() + ", you just won " + coinsAmount + " coins!");
        }
        StringBuilder stringBuilder = new StringBuilder("Congratulations, ");
        int i = 0;
        for (Map.Entry<Statistics.Viewer, Integer> entry : raidResult.entrySet()) {
            if (i > 20) {
                break;
            }
            stringBuilder.append(entry.getKey().getName() + ", ");
            i++;
        }
        stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length() - 1);
        if (i > 20) {
            stringBuilder.append(" and others!");
        }
        bot.sendMessage(stringBuilder.toString());
    }

    public Map<Statistics.Viewer, Integer> getRaidResult() {
        return raidResult;
    }
}