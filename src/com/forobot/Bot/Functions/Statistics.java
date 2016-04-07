package com.forobot.Bot.Functions;

import com.forobot.Bot.Handlers.LogHandler;
import com.forobot.Utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * This class handles all the statistics functions. Such as: Most active viewers Amount of total
 * messages in the chat Message log and retrieving various information from it
 */
public class Statistics {
    private static final ConcurrentHashMap<String, Viewer> ALL_TIME_VIEWERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Viewer> CURRENT_SESSION_VIEWERS = new ConcurrentHashMap<>();

    private static final List<Message> messageLog = Collections.synchronizedList(new ArrayList<Message>());

    private static AtomicInteger messageCount = new AtomicInteger(0);

    private Statistics() {
    }

    /**
     * Viewer list is located in file, each line with one viewer in this format :
     * "viewername=messagescount"
     */
    public static void initializeViewersListFromFile(String filename) {
        ArrayList<String> lines = FileUtils.readAllLinesFromFile(filename);
        for (String line : lines) {
            String[] partsOfLine = line.split("=");
            String[] params = partsOfLine[1].split(";");
            String name = partsOfLine[0];
            int messageAmount = Integer.parseInt(params[0]);
            int moneyAmount = Integer.parseInt(params[1]);

            Viewer viewer = new Viewer(name, messageAmount, moneyAmount);
            ALL_TIME_VIEWERS.put(name, viewer);
        }
    }

    public static void saveViewersListIntoAFile(String filename) {
        ArrayList<String> lines = new ArrayList<>();
        for (Map.Entry<String, Viewer> entry : ALL_TIME_VIEWERS.entrySet()) {
            Viewer viewer = entry.getValue();
            lines.add(viewer.toString());
        }
        FileUtils.writeAllLinesToTheFile(lines, filename);
    }

    public static void increaseAmountOfMessages(String name, String message) {
        name = name.toLowerCase();
        if (!name.endsWith("bot")) {
            if (ALL_TIME_VIEWERS.containsKey(name)) {
                int oldAmount = ALL_TIME_VIEWERS.get(name).getMessageCount();
                int newAmount = oldAmount + 1;
                ALL_TIME_VIEWERS.get(name).setMessageCount(newAmount);
                if (!message.startsWith("!")) {
                    increaseCoinsAmount(name, 5);
                }
            } else {
                ALL_TIME_VIEWERS.put(name, new Viewer(name, 1, 5));
            }

            if (CURRENT_SESSION_VIEWERS.containsKey(name)) {
                int oldAmount = CURRENT_SESSION_VIEWERS.get(name).getMessageCount();
                int newAmount = oldAmount + 1;
                CURRENT_SESSION_VIEWERS.get(name).setMessageCount(newAmount);
            } else {
                CURRENT_SESSION_VIEWERS.put(name, new Viewer(name, 1));
            }
        }
        increaseTheMessageCount();
        logTheMessage(name, message);
    }

    public static void increaseCoinsAmount(String name, int amount) {
        name = name.toLowerCase();
        ALL_TIME_VIEWERS.get(name).setMoneyAmount(ALL_TIME_VIEWERS.get(name).getMoneyAmount() + amount);
    }

    public static void decreaseCoinsAmount(String name, int amount) {
        if (!hasEnoughMoney(name, amount)){
            return;
        }
        name = name.toLowerCase();
        ALL_TIME_VIEWERS.get(name).setMoneyAmount(ALL_TIME_VIEWERS.get(name).getMoneyAmount() - amount);
    }

    public static boolean hasEnoughMoney(String name, int amount){
        name = name.toLowerCase();
        int currentAmount = ALL_TIME_VIEWERS.get(name).getMoneyAmount();
        if (currentAmount < amount){
            return false;
        }
        return true;
    }

    public static Viewer getMostActiveViewerOfAllTime() {
        int maxAmount = 0;
        String maxName = null;
        Viewer mostActiveViewer = null;
        for (Map.Entry<String, Viewer> entry : ALL_TIME_VIEWERS.entrySet()) {
            if (entry.getValue().getMessageCount() > maxAmount) {
                maxAmount = entry.getValue().getMessageCount();
                maxName = entry.getKey();
                mostActiveViewer = new Viewer(maxName, maxAmount);
            }
        }
        return mostActiveViewer;
    }

    public static boolean isAnActiveViewer(String viewer){
        viewer = viewer.toLowerCase();
        return ALL_TIME_VIEWERS.containsKey(viewer);
    }

    public static ArrayList<Viewer> getTopViewersAllTime(int count) {
        ArrayList<Viewer> arrayList = new ArrayList<>(count);

        ArrayList<Viewer> allViewers = new ArrayList<>(ALL_TIME_VIEWERS.size());
        for (Map.Entry<String, Viewer> entry : ALL_TIME_VIEWERS.entrySet()) {
            Viewer viewer = entry.getValue();
            allViewers.add(viewer);
        }
        Collections.sort(allViewers);

        for (int i = 0; i < count; i++) {
            arrayList.add(allViewers.get(i));
        }
        return arrayList;
    }

    public static ArrayList<Viewer> getTopViewersOfSession(int count) {
        ArrayList<Viewer> arrayList = new ArrayList<>(count);

        ArrayList<Viewer> allViewers = new ArrayList<>(CURRENT_SESSION_VIEWERS.size());
        for (Map.Entry<String, Viewer> entry : CURRENT_SESSION_VIEWERS.entrySet()) {
            Viewer viewer = entry.getValue();
            allViewers.add(viewer);
        }
        Collections.sort(allViewers);

        for (int i = 0; i < count; i++) {
            arrayList.add(allViewers.get(i));
        }
        return arrayList;
    }

    public static Viewer getMostActiveViewerOfCurrentSession() {
        int maxAmount = 0;
        String maxName = null;
        Viewer mostActiveViewer = null;
        for (Map.Entry<String, Viewer> entry : CURRENT_SESSION_VIEWERS.entrySet()) {
            if (entry.getValue().getMessageCount() > maxAmount) {
                maxAmount = entry.getValue().getMessageCount();
                maxName = entry.getKey();
                mostActiveViewer = new Viewer(maxName, maxAmount);
            }
        }
        return mostActiveViewer;
    }

    public static Viewer getRichestViewer(){
        int count = 0;
        String name = "";
        for (Map.Entry<String, Viewer> entry : ALL_TIME_VIEWERS.entrySet()){
            if (entry.getValue().getMoneyAmount() > count){
                name = entry.getKey();
                count = entry.getValue().getMoneyAmount();
            }
        }
        if (name.equals("")){
            return null;
        }
        return ALL_TIME_VIEWERS.get(name);
    }

    public static void increaseTheMessageCount() {
        messageCount.incrementAndGet();
    }

    private static void logTheMessage(String sender, String message){
        new Thread(() -> {
            messageLog.add(new Message(sender, message));
        }).start();
    }

    public static List<Message> getMessagesForUser(String sender){
        sender = sender.toLowerCase();
        ArrayList<Message> messages = new ArrayList<>();
        boolean isValidUser = false;
        synchronized (messageLog) {
            for (Message message : messageLog) {
                if (message.getSender().equals(sender)) {
                    isValidUser = true;
                    messages.add(message);
                }
            }
        }
        if (!isValidUser){
            return null;
        }
        return messages;
    }

    public static int getMessageCount() {
        return messageCount.get();
    }

    public boolean isReady() {
        return ALL_TIME_VIEWERS.size() > 0;
    }

    public static Viewer getViewer(String name){
        name = name.toLowerCase();
        if (!ALL_TIME_VIEWERS.containsKey(name)){
            return null;
        }
        return ALL_TIME_VIEWERS.get(name);
    }

    public static class Viewer implements Comparable<Viewer> {
        private String name;
        private int messageCount;
        private int moneyAmount;

        public Viewer(String name) {
            this.name = name;
            messageCount = 0;
        }

        public Viewer(String name, int messageCount) {
            this.name = name;
            this.messageCount = messageCount;
        }

        public Viewer(String name, int messageCount, int moneyAmount) {
            this.name = name;
            this.messageCount = messageCount;
            this.moneyAmount = moneyAmount;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Viewer)) {
                return false;
            }
            Viewer viewer = (Viewer) obj;
            return viewer.getName().equals(this.getName());
        }

        @Override
        public String toString() {
            String result = this.name + "=" + this.messageCount + ";" +  this.moneyAmount;
            return result;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMessageCount() {
            return messageCount;
        }

        public void setMessageCount(int messageCount) {
            this.messageCount = messageCount;
        }

        public int getMoneyAmount() {
            return moneyAmount;
        }

        public void setMoneyAmount(int moneyAmount) {
            this.moneyAmount = moneyAmount;
        }

        @Override
        public int compareTo(Viewer o) {
            if (o.messageCount != this.messageCount) {
                return o.messageCount - this.getMessageCount();
            }
            if (o.getName().length() != this.getName().length()) {
                return o.getName().length() - this.getName().length();
            }
            return o.getName().compareTo(this.getName());
        }


    }

    public static class Message {
        private final String sender;
        private final String message;

        public Message(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "sender='" + sender + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public static class Refresher implements Runnable {
        private Label allTimeLabel;
        private Label sessionLabel;
        private Label messageAmountLabel;
        private Label richestViewerLabel;

        public Refresher(Label allTimeLabel, Label sessionLabel, Label messageAmountLabel, Label richestViewerLabel) {
            this.allTimeLabel = allTimeLabel;
            this.sessionLabel = sessionLabel;
            this.messageAmountLabel = messageAmountLabel;
            this.richestViewerLabel = richestViewerLabel;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Viewer mostActiveAllTimeViewer = Statistics.getMostActiveViewerOfAllTime();
                String mostActiveAllTimeName = null;
                String mostActiveAllTimeCount = null;
                if (mostActiveAllTimeViewer != null) {
                    mostActiveAllTimeName = mostActiveAllTimeViewer.getName();
                    mostActiveAllTimeCount = String.valueOf(mostActiveAllTimeViewer.getMessageCount());
                }

                Viewer mostActiveSessionViewer = Statistics.getMostActiveViewerOfCurrentSession();
                String mostActiveSessionName = null;
                String mostActiveSessionCount = null;

                if (mostActiveSessionViewer != null) {
                    mostActiveSessionName = mostActiveSessionViewer.getName();
                    mostActiveSessionCount = String.valueOf(mostActiveSessionViewer.getMessageCount());
                }

                String mostActiveAllTime;
                String mostActiveSession;
                String messageAmount = String.valueOf(getMessageCount());
                String richest;

                if (mostActiveAllTimeName == null) {
                    mostActiveAllTime = "nobody yet!";
                    richest = "nobody yet!";
                } else {
                    mostActiveAllTime = String.format("%s with %s messages!", mostActiveAllTimeName, mostActiveAllTimeCount);
                    Viewer richestViewer = getRichestViewer();
                    richest = String.format("%s with %d coins", richestViewer.getName(), richestViewer.getMoneyAmount());
                }

                if (mostActiveSessionName == null) {
                    mostActiveSession = "nobody yet!";
                } else {
                    mostActiveSession = String.format("%s with %s messages!", mostActiveSessionName, mostActiveSessionCount);
                }


                Platform.runLater(() -> {
                    allTimeLabel.setText(mostActiveAllTime);
                    sessionLabel.setText(mostActiveSession);
                    messageAmountLabel.setText(messageAmount);
                    richestViewerLabel.setText(richest);
                });
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
