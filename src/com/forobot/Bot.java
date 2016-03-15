package com.forobot;

import org.jibble.pircbot.PircBot;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

//TODO: If options file doesn't exist, create it.
//TODO: Update close() method



/**
 * This class handles all the main functionality of bot, not including the console commands.
 */

public class Bot extends PircBot {
    private String viewerlistUrl = "https://tmi.twitch.tv/group/user/foreseer_/chatters";
    private String followsUrl = "https://api.twitch.tv/kraken/channels/foreseer_/follows?limit=1";

    private final String OPTIONS_PATH = "D:\\options.ini";

    private String channelName;
    private String adminName;

    private SpeechSynthesizer speechSynthesizer;
    private CommandHandler commandHandler;
    private JSONParser jsonParser;
    private SpamChecker spamChecker;
    private ConsoleHandler consoleHandler;

    private ArrayList<String> viewerNicknames = null;
    private String lastFollower = null;

    private boolean parseChatCommands = false;


    public Bot(String name, String channelName) {
        this.setLogin(name);
        this.setName(name);
        this.channelName = channelName;
        this.adminName = channelName.substring(1);
        speechSynthesizer = new SpeechSynthesizer();
        commandHandler = new CommandHandler(OPTIONS_PATH);
        spamChecker = new SpamChecker(OPTIONS_PATH);
        jsonParser = new JSONParser();
    }

    public void initialiseConsoleHandler(ConsoleHandler consoleHandler) {
        consoleHandler.setBot(this);
        this.consoleHandler = consoleHandler;

        consoleHandler.setCommandHandler(commandHandler);
        consoleHandler.setSpeechSynthesizer(speechSynthesizer);
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        System.out.println("Message : " + message);
        System.out.println("Sender : " + sender);
        System.out.println("Channel : " + channel);
        System.out.println();

        try {
            if (sender.equals(adminName)) {
                if (consoleHandler.isConsoleCommand(message)) {
                    consoleHandler.handleCommand(message);
                }
            }

            if (commandHandler.isCommand(message) && !sender.equals(adminName)) {
                if (parseChatCommands) {
                    sendMessage(channelName, commandHandler.getResponse(message));
                } else {
                    speechSynthesizer.spell(sender, "command");
                }
            } else if (!spamChecker.isSpam(message) || sender.equals(adminName)) {
                speechSynthesizer.spell(sender, message);
            } else {
                speechSynthesizer.spell(sender, "spam");
                handleSpam(sender, message, 120);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        System.out.println();
        System.out.println(login + " joined " + channel);
    }

    /**
     * Replaces all emotes(excluding Kappa) in the message to blank spaces
     *
     * @param message the message that someone sent to the chat
     * @return message without emotes
     */
    private String replaceEmotes(String message) {
        return null;
    }

    /**
     * This method returns ArrayList with nicknames of viewers, used in checkForNewViewers method
     *
     * @return ArrayList<String> with nicknames
     */
    private ArrayList<String> getViewers() {
        ArrayList<String> viewersList = new ArrayList<>();
        //build a JSON Object
        JSONObject jsonObject = null;
        try {
            jsonObject = jsonParser.parseJsonFromUrl(viewerlistUrl);
        } catch (JSONParser.ParsingException502 e) {
            System.out.println("There was a 502/503 error parsing the viewer page.");
        }
        //get chatters array
        if (jsonObject == null) {
            return null;
        }
        JSONObject chatters = jsonObject.getJSONObject("chatters");
        //get viewers and moderators JSONArrays
        JSONArray jsonViewersArray = chatters.getJSONArray("viewers");
        JSONArray jsonModeratorsArray = chatters.getJSONArray("moderators");

        //add all nicknames to viewersList
        for (int i = 0; i < jsonViewersArray.length(); i++) {
            viewersList.add(jsonViewersArray.get(i).toString());
        }
        for (int i = 0; i < jsonModeratorsArray.length(); i++) {
            viewersList.add(jsonModeratorsArray.get(i).toString());
        }

        return viewersList;

    }

    public void checkLastFollower() {
        JSONParser JSONParser = new JSONParser();
        JSONObject followObject = null;
        try {
            followObject = JSONParser.parseJsonFromUrl(followsUrl);
        } catch (JSONParser.ParsingException502 parsingException502) {
            System.out.println("There was a 502/503 error parsing the follower page.");
        }
        JSONArray follows = followObject.getJSONArray("follows");
        JSONObject user = follows.getJSONObject(0);
        JSONObject name = user.getJSONObject("user");
        String follower = name.getString("name");
        if (lastFollower == null) {
            lastFollower = follower;
        } else {
            if (!lastFollower.equals(follower)) {
                System.out.println(String.format("New follower %s !", follower));
                sendMessage(channelName, String.format("Thanks for follow, %s!", follower));
                lastFollower = follower;
            }
        }
    }

    /**
     * This method checks whether new users joined the chat
     */
    public void checkForNewViewers() {
        if (viewerNicknames == null) {
            viewerNicknames = getViewers();
        } else {
            ArrayList<String> newViewerNicknamesList = getViewers();
            if (newViewerNicknamesList != null) {
                for (String nickname : newViewerNicknamesList) {
                    if (!viewerNicknames.contains(nickname)) {
                        System.out.println(String.format("%s joined %s", nickname, channelName));
                        System.out.println();
                        sendMessage(channelName, String.format("Welcome, %s!", nickname));
                    }
                }
                viewerNicknames = newViewerNicknamesList;
            }
        }
    }

    public void addNewProhibitedWord(String word) {
        spamChecker.addNewProhibitedWord(word);
    }

    public void removeExistingProhibitedWord(String word) {
        spamChecker.removeExistingProhibitedWord(word);
    }

    public void handleSpam(String sender, String message, int durationOfTimeout) throws InterruptedException {
        String timeoutMessage = String.format(".timeout %s %d", sender, durationOfTimeout);
        String shortTimeoutMessage = String.format(".timeout %s %d", sender, 1);

        if (spamChecker.containsTooMuchCaps(message)) {
            String response = String.format("Don't use too many capital letters in your message, %s", sender);

            sendMessage(channelName, response);
            sendMessage(channelName, timeoutMessage);

        } else if (spamChecker.containsConsecutiveRepeatingCharacters(message)) {
            String response = String.format("Don't use too many consecutive repeating characters in your message, %s", sender);

            sendMessage(channelName, response);
            sendMessage(channelName, timeoutMessage);
        } else if (spamChecker.containsProhibitedWords(message)) {
            String response = String.format("Don't use words that are not permitted, %s", sender);

            sendMessage(channelName, response);
            sendMessage(channelName, shortTimeoutMessage);
        }

    }

    public void setParseChatCommands(boolean parseChatCommands) {
        if (parseChatCommands) {
            System.out.println("Chat commands parsing mode is ON.");
        } else {
            System.out.println("Chat commands parsing mode is OFF.");
        }
        this.parseChatCommands = parseChatCommands;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
        this.adminName = this.channelName.substring(1);
        viewerlistUrl = String.format("https://tmi.twitch.tv/group/user/%s/chatters", adminName);
        followsUrl = String.format("https://api.twitch.tv/kraken/channels/%s/follows?limit=1", adminName);
    }

    public void close() {
        speechSynthesizer.close();
    }
}
