package com.forobot.Bot;

import com.forobot.Bot.Functions.Statistics;
import com.forobot.Bot.Handlers.CommandHandler;
import com.forobot.Bot.Handlers.ConsoleHandler;
import com.forobot.Bot.Handlers.LogHandler;
import com.forobot.Bot.Handlers.SpamChecker;
import com.forobot.Utils.JSONParser;
import com.forobot.Utils.TwitchUtils;

import org.jibble.pircbot.PircBot;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;


/**
 * This class handles all the main functionality of bot.
 */

public class Bot extends PircBot {
    private final String OPTIONS_PATH;
    private final String VIEWERS_FILE_PATH;
    private final String APP_PATH;

    private String viewerlistUrl;
    private String followsUrl;
    private String channelName;
    private String adminName;

    private SpeechSynthesizer speechSynthesizer;
    private CommandHandler commandHandler;
    private JSONParser jsonParser;
    private SpamChecker spamChecker;
    private ConsoleHandler consoleHandler;

    private ArrayList<String> viewerNicknames = null;
    private ArrayList<String> emotes = null;
    private String lastFollower = null;

    private boolean parseChatCommands = false;
    private boolean greetNewViewers = false;
    private boolean spellOutMessages = true;

    private int durationOfBan = 120;


    public Bot(String name, String channelName, String optionsPath, String viewerListPath) {
        this.setLogin(name);
        this.setName(name);


        this.channelName = channelName;
        this.adminName = channelName.substring(1);
        this.OPTIONS_PATH = optionsPath;
        this.VIEWERS_FILE_PATH = viewerListPath;

        this.APP_PATH = System.getProperty("user.dir");

        this.viewerlistUrl = String.format("https://tmi.twitch.tv/group/user/%s/chatters", channelName.substring(1));
        this.followsUrl = String.format("https://api.twitch.tv/kraken/channels/%s/follows?limit=1", channelName.substring(1));

        emotes = TwitchUtils.retrieveEmotesList();

        speechSynthesizer = new SpeechSynthesizer();
        commandHandler = new CommandHandler(OPTIONS_PATH);
        spamChecker = new SpamChecker(OPTIONS_PATH, false);
        jsonParser = new JSONParser();
    }

    public void initialiseConsoleHandler(ConsoleHandler consoleHandler) {
        consoleHandler.setBot(this);
        this.consoleHandler = consoleHandler;
    }

    public boolean isParseChatCommands() {
        return parseChatCommands;
    }

    public void setParseChatCommands(boolean parseChatCommands) {
        if (parseChatCommands) {
            LogHandler.log("Chat commands parsing mode is ON.");
        } else {
            LogHandler.log("Chat commands parsing mode is OFF.");
        }
        this.parseChatCommands = parseChatCommands;
    }

    public String getAPP_PATH() {
        return APP_PATH;
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        //String information = String.format("%s send the message to the channel %s : \"%s\"", sender, channel, message);
        //LogHandler.log(information);

        Statistics.increaseAmountOfMessages(sender);

        LocalDateTime time = LocalDateTime.now();

        StringBuilder nowString = new StringBuilder();
        nowString.append(time.getHour())
                 .append(":")
                 .append(time.getMinute());

        LogHandler.logSilently(nowString.toString() + " : " + sender + " : " + message);

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
                    asynchronousSpell(sender, "command");
                }
            } else if (!spamChecker.isSpam(message) || sender.equals(adminName)) {
                asynchronousSpell(sender, replaceEmotes(message));
            } else {
                asynchronousSpell(sender, "spam");
                handleSpam(sender, message, durationOfBan);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replaces all emotes(excluding Kappa) in the message to blank spaces
     *
     * @param message the message that someone sent to the chat
     * @return message without emotes
     */
    private String replaceEmotes(String message) {
        for (String emote : emotes){
            if (message.contains(emote)){
                message = message.replace(emote, "");
            }
        }
        return message;
    }

    private void asynchronousSpell(String sender, String message) {
        if (this.spellOutMessages) {
            new Thread(() -> {
                try {
                    speechSynthesizer.spell(sender, message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * This method returns ArrayList with nicknames of ALL_TIME_VIEWERS, used in checkForNewViewers
     * method
     *
     * @return ArrayList<String> with nicknames
     */
    private ArrayList<String> getViewers() {
        ArrayList<String> viewersList = new ArrayList<>();
        //build a JSON Object
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONParser.parseJsonFromUrl(viewerlistUrl);
        } catch (JSONParser.ParsingException502 e) {
            LogHandler.log("There was a 502/503 error parsing the viewer page.");
        }
        //get chatters array
        if (jsonObject == null) {
            return null;
        }
        JSONObject chatters = jsonObject.getJSONObject("chatters");
        //get ALL_TIME_VIEWERS and moderators JSONArrays
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
        JSONObject followObject = null;
        try {
            followObject = JSONParser.parseJsonFromUrl(followsUrl);
        } catch (JSONParser.ParsingException502 parsingException502) {
            LogHandler.log("There was a 502/503 error parsing the follower page.");
        }
        JSONArray follows = followObject.getJSONArray("follows");
        JSONObject user = follows.getJSONObject(0);
        JSONObject name = user.getJSONObject("user");

        String follower = name.getString("name");

        if (lastFollower == null) {
            lastFollower = follower;
        } else {
            if (!lastFollower.equals(follower)) {
                LogHandler.log(String.format("New follower %s !", follower));
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
                        LogHandler.log(String.format("%s joined %s", nickname, channelName));
                        LogHandler.log("");
                        if (greetNewViewers) {
                            sendMessage(channelName, String.format("Welcome, %s!", nickname));
                        }
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
        } else if (spamChecker.containsLinks(message)){
            String response = String.format("Links are not permitted, %s", sender);

            sendMessage(channelName, response);
            sendMessage(channelName, shortTimeoutMessage);
        }

    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
        this.adminName = this.channelName.substring(1);
        viewerlistUrl = String.format("https://tmi.twitch.tv/group/user/%s/chatters", adminName);
        followsUrl = String.format("https://api.twitch.tv/kraken/channels/%s/follows?limit=1", adminName);
    }

    public SpamChecker getSpamChecker() {
        return spamChecker;
    }

    public ConsoleHandler getConsoleHandler() {
        return consoleHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public boolean isGreetNewViewers() {
        return greetNewViewers;
    }

    public void setGreetNewViewers(boolean greetNewViewers) {
        this.greetNewViewers = greetNewViewers;
        LogHandler.log("Viewers greeting mode is " + (greetNewViewers ? "ON" : "OFF"));
    }

    public boolean isSpellOutMessages() {
        return spellOutMessages;
    }

    public void setSpellOutMessages(boolean spellOutMessages) {
        this.spellOutMessages = spellOutMessages;
        speechSynthesizer.setSpellMessages(spellOutMessages);
        LogHandler.log("Spelling mode is " + (spellOutMessages ? "ON" : "OFF"));
    }

    public SpeechSynthesizer getSpeechSynthesizer() {
        return speechSynthesizer;
    }

    public JSONParser getJsonParser() {
        return jsonParser;
    }

    public void setDurationOfBan(int durationOfBan) {
        this.durationOfBan = durationOfBan;
    }

    public void close() {
        speechSynthesizer.close();
        Statistics.saveViewersListIntoAFile(VIEWERS_FILE_PATH);
    }
}
