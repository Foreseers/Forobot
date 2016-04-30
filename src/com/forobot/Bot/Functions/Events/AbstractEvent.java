package com.forobot.Bot.Functions.Events;

import com.forobot.Bot.Bot;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Konstantin on 29.04.2016.
 */
public abstract class AbstractEvent implements Runnable {
    protected EventHandler.EventType type;
    protected int duration;
    protected AtomicBoolean finished;
    protected int clock;

    protected Bot bot;

    public AbstractEvent(EventHandler.EventType type, int duration, Bot bot) {
        this.type = type;
        this.duration = duration;
        this.bot = bot;
        finished = new AtomicBoolean(false);
        clock = 0;
    }

    public boolean isFinished(){
        return finished.get();
    }
}
