package com.forobot.Bot.Functions;

import com.forobot.Utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Created by Foreseer on 18.03.2016.
 */
public class Statistics {
    private static final ConcurrentHashMap<String, Integer> ALL_TIME_VIEWERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> CURRENT_SESSION_VIEWERS = new ConcurrentHashMap<>();
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
            String name = partsOfLine[0];
            int amount = Integer.parseInt(partsOfLine[1]);
            ALL_TIME_VIEWERS.put(name, amount);
        }
    }

    public static void saveViewersListIntoAFile(String filename) {
        ArrayList<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : ALL_TIME_VIEWERS.entrySet()) {
            String name = entry.getKey();
            String amount = String.valueOf(entry.getValue());
            String output = String.format("%s=%s", name, amount);
            lines.add(output);
        }
        FileUtils.writeAllLinesToTheFile(lines, filename);
    }

    public static void increaseAmountOfMessages(String name) {
        if (!name.endsWith("bot")) {
            if (ALL_TIME_VIEWERS.containsKey(name)) {
                int oldAmount = ALL_TIME_VIEWERS.get(name);
                int newAmount = oldAmount + 1;
                ALL_TIME_VIEWERS.replace(name, newAmount);
            } else {
                ALL_TIME_VIEWERS.put(name, 1);
            }

            if (CURRENT_SESSION_VIEWERS.containsKey(name)) {
                int oldAmount = CURRENT_SESSION_VIEWERS.get(name);
                int newAmount = oldAmount + 1;
                CURRENT_SESSION_VIEWERS.replace(name, newAmount);
            } else {
                CURRENT_SESSION_VIEWERS.put(name, 1);
            }
        }
        increaseTheMessageCount();
    }

    public static Viewer getMostActiveViewerOfAllTime() {
        int maxAmount = 0;
        String maxName = null;
        Viewer mostActiveViewer = null;
        for (Map.Entry<String, Integer> entry : ALL_TIME_VIEWERS.entrySet()) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
                maxName = entry.getKey();
                mostActiveViewer = new Viewer(maxName, maxAmount);
            }
        }
        return mostActiveViewer;
    }

    public static ArrayList<Viewer> getTopViewersAllTime(int count) {
        ArrayList<Viewer> arrayList = new ArrayList<>(count);

        ArrayList<Viewer> allViewers = new ArrayList<>(ALL_TIME_VIEWERS.size());
        for (Map.Entry<String, Integer> entry : ALL_TIME_VIEWERS.entrySet()) {
            Viewer viewer = new Viewer(entry.getKey(), entry.getValue());
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
        for (Map.Entry<String, Integer> entry : CURRENT_SESSION_VIEWERS.entrySet()) {
            Viewer viewer = new Viewer(entry.getKey(), entry.getValue());
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
        for (Map.Entry<String, Integer> entry : CURRENT_SESSION_VIEWERS.entrySet()) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
                maxName = entry.getKey();
                mostActiveViewer = new Viewer(maxName, maxAmount);
            }
        }
        return mostActiveViewer;
    }

    public static void increaseTheMessageCount() {
        messageCount.incrementAndGet();
    }

    public static int getMessageCount() {
        return messageCount.get();
    }

    public boolean isReady() {
        return ALL_TIME_VIEWERS.size() > 0;
    }

    public static class Viewer implements Comparable<Viewer> {
        private String name;
        private int messageCount;

        public Viewer(String name) {
            this.name = name;
            messageCount = 0;
        }

        public Viewer(String name, int messageCount) {
            this.name = name;
            this.messageCount = messageCount;
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
            String result = this.name + "=" + this.messageCount;
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


        @Override
        public int compareTo(Viewer o) {
            if (o.messageCount != this.messageCount) {
                return o.messageCount - this.getMessageCount();
            }
            if (o.getName().length() != this.getName().length()){
                return o.getName().length() - this.getName().length();
            }
            return o.getName().compareTo(this.getName());
        }


    }

    public static class Refresher implements Runnable {
        private Label allTimeLabel;
        private Label sessionLabel;
        private Label messageAmountLabel;

        public Refresher(Label allTimeLabel, Label sessionLabel, Label messageAmountLabel) {
            this.allTimeLabel = allTimeLabel;
            this.sessionLabel = sessionLabel;
            this.messageAmountLabel = messageAmountLabel;
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

                if (mostActiveAllTimeName == null) {
                    mostActiveAllTime = "nobody yet!";
                } else {
                    mostActiveAllTime = String.format("%s with %s messages!", mostActiveAllTimeName, mostActiveAllTimeCount);
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
