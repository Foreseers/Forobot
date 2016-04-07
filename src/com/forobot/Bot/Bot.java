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
        //Sets the username under which bot will try to login to the server.
        this.setLogin(name);
        //Sets the nickname the bot will use on IRC server, on twitch is the same of username.
        this.setName(name);


        this.channelName = channelName;
        this.adminName = channelName.substring(1);
        this.OPTIONS_PATH = optionsPath;
        this.VIEWERS_FILE_PATH = viewerListPath;

        //Get absolute path to app's directory.
        this.APP_PATH = System.getProperty("user.dir");

        //Build up urls for viewer list parsing and followers parsing
        //Might be deprecated now as those features don't work as intended.
        this.viewerlistUrl = String.format("https://tmi.twitch.tv/group/user/%s/chatters", channelName.substring(1));
        this.followsUrl = String.format("https://api.twitch.tv/kraken/channels/%s/follows?limit=1", channelName.substring(1));

        //Get the list of emotes from Twitch.
        emotes = TwitchUtils.retrieveEmotesList();

        speechSynthesizer = new SpeechSynthesizer();
        commandHandler = new CommandHandler(OPTIONS_PATH, adminName);
        spamChecker = new SpamChecker(OPTIONS_PATH, false);
        jsonParser = new JSONParser();
    }

    /**
     * This method is called every time a new message is recieved. Essentially all bot's main
     * functionality is handled in this method. Bot will pass all the information about the recieved
     * message as params when calling this method.
     *
     * @param channel  Channel in which the message was sent.
     * @param sender   Nickname of the user that sent the message.
     * @param login    Username of the user that sent the message. The same as nickname on Twitch.
     * @param hostname Hostname of the user that sent the message. It's in following format :
     *                 "user.tmi.twitch.tv", where "user" is the nickname of the sender.
     * @param message  Message that the user sent.
     */
    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {

        //Increase the amount of message for statistics module.
        Statistics.increaseAmountOfMessages(sender, message);

        //Get current time for logging.
        LocalDateTime time = LocalDateTime.now();

        sender = sender.toLowerCase();

        //Build up the time string for logging.
        StringBuilder nowString = new StringBuilder();
        nowString.append(time.getHour())
                .append(":")
                .append(time.getMinute());

        //Silently log the recieved message.
        LogHandler.logSilently(nowString.toString() + " : " + sender + " : " + message);

        //If the message is a command, we respond it if the parseChatCommands boolean is true.
        //If we currently don't respond to commands, we spell it out like "user said command".
        //If it's not a command, check it for spam.
        //If it doesn't contain spam or the sender is channel admin, spell it out.
        if (commandHandler.isCommand(message.split(" ")[0])) {
            if (parseChatCommands) {
                //Respond the message in chat
                commandHandler.respondCommand(this, sender, message);
            } else {
                //Spell it out like "user said command" otherwise.
                asynchronousSpell(sender, "command");
            }
            //Check whether the message if spam or not.
        } else if (!spamChecker.isSpam(message)) {
            //Spell it out
            asynchronousSpell(sender, replaceEmotes(message));
        } else {
            //Spell it out like "user said spam" if it's spam..
            asynchronousSpell(sender, "spam");
            //Ban the user
            if (!sender.equals(adminName)) {
                handleSpam(sender, message, durationOfBan);
            }
        }
    }

    /**
     * Replaces all emotes(excluding Kappa) in the message to blank spaces
     *
     * @param message the message that someone sent to the chat
     * @return message without emotes
     */
    private String replaceEmotes(String message) {
        for (String emote : emotes) {
            if (message.contains(emote)) {
                message = message.replace(emote, "");
            }
        }
        return message;
    }

    public void sendMessage(String message){
        sendMessage(channelName, message);
    }

    /**
     * Asynchronously call the spell method of speechSynthesizer. The reason we need to do it in
     * another thread is that otherwise speechSynthesizer would block out our current thread until
     * the message is spelled out.
     *
     * @param sender  Sender of the message.
     * @param message Message that needs to be spelled out.
     */
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

        if (jsonObject == null) {
            return null;
        }
        //get chatters array
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

    /**
     * Check whether the channel got new followers and greet them if such appeared. This method will
     * stay here temporarily as better alternatives such as TwitchAlerts exist, and it's not working
     * as intended anyways.
     */
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
     * This method checks whether there are new users in chat or not. Currently not working as
     * intended due to Twitch's API failure. Will probably be removed or reworked, currently leaving
     * it be as a temporary alternative; at last, it's not very useful anyways besides when the
     * channel only has very small amount of viewers.
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

    /**
     * This method handles spam by banning out the user or simply timing him out for 1 second to
     * remove the spam message.
     *
     * @param sender            User that sent the message.
     * @param message           Message that the user sent.
     * @param durationOfTimeout Duration for which the user will be timed out.
     */
    public void handleSpam(String sender, String message, int durationOfTimeout) {
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
        } else if (spamChecker.containsLinks(message)) {
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

    public JSONParser getJsonParser() {
        return jsonParser;
    }

    public void setDurationOfBan(int durationOfBan) {
        this.durationOfBan = durationOfBan;
    }

}
