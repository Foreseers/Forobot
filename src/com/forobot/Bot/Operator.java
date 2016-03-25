package com.forobot.Bot;

import com.forobot.Bot.Functions.Statistics;
import com.forobot.Bot.Handlers.ConsoleHandler;
import com.forobot.Bot.Handlers.LogHandler;
import com.forobot.GUI.GUIController;
import com.forobot.Utils.FileUtils;
import com.forobot.Utils.TwitchUtils;

import org.jibble.pircbot.IrcException;

import java.io.IOException;

import javafx.application.Platform;

//This is a general TO DO list.
//TODO: Request music from youtube (low priority, hard to implement)
//TODO: Polls
//TODO: Adding time onto a counter for marathons per each subscribe/donation.
//TODO: Custom API support (nightbot has it I guess?)
//TODO: Decent logging system needs to be implemented.

/**
 * This class organises the work of bot-related classes and makes them work together.
 *
 */

/*
 *  Available console commands:
 * !add !initiator response | adds a new chat command available to request via chat
 * !remove !initiator       | removes an existing chat command !list | lists all console commands
 * !parse true/false        | sets chat parsing mode to true of false(default: false)
 * !spamAdd word            | adds a new word to the blacklist
 * !spamRemove word         | removes an existing word from the blacklist
 */

public class Operator implements Runnable {
    //Bot will login under this username.
    private final String BOT_TWITCH_NICKNAME = "foreseerbot";
    //Twitch IRC address.
    private final String IRC_ADDRESS;
    //Twitch IRC port.
    private final int PORT;
    //Authentication token, used for authentication on twitch.
    private final String AUTH_TOKEN = "oauth:n4wixjncrxpigyi9c5qrzt2saqnq47";

    //Absolute path to the application, will be initialised in the constructor.
    private final String APP_PATH;

    //Name of sections, will be used to create/read the file with sections
    private final String COMMANDS_SECTION = "ChatCommands";
    private final String BLACKLIST_SECTION = "Blacklist";

    //Paths to options and viewers files, are initialised during constructor call.
    private final String OPTIONS_FILEPATH;
    private final String VIEWERS_FILEPATH;

    //Channel name that bot will connect to, can be changed after start of the program by user.
    private String CHANNEL_NAME = "#foreseer_";
    //Used during check of viewer count.
    //If the amount of viewers is too high, certain function will be disabled.
    //As checking the viewer count on Twitch requires making an API call, it shouldn't be done twice
    //for the sake of performance.
    private boolean checked = false;
    //The bot, will be initialised in the constructor
    private Bot bot;

    //JavaFX Controller via which we are accessing gui elements.
    private GUIController controller;

    public Operator(String channelName, boolean parseChatCommands, GUIController controller) {
        //Channel name comes without an "#" that we need to connect to the IRC, so we append it
        this.CHANNEL_NAME = String.format("#%s", channelName);

        this.controller = controller;

        //Getting an absolute path to the folder with application.
        APP_PATH = System.getProperty("user.dir");
        //Saving channel options to separate folder of the channel.
        String userFolder = APP_PATH + "\\" + channelName;
        //Create a folder if doesn't exist.
        if (!FileUtils.isExistingFile(userFolder)){
            FileUtils.createAFolder(APP_PATH + "\\", channelName);
        }
        //Initialising filepath variables.
        this.OPTIONS_FILEPATH = String.format("%s\\%s\\options.ini", APP_PATH, channelName);
        this.VIEWERS_FILEPATH = String.format("%s\\%s\\viewers.foo", APP_PATH, channelName);

        //Create a new options file with sections if doesn't exist.
        if (!FileUtils.isExistingFile(OPTIONS_FILEPATH)) {
            FileUtils.createASectionsFile(OPTIONS_FILEPATH, COMMANDS_SECTION, BLACKLIST_SECTION);
        }

        //Create a new viewerlist file if it doesn't exist.
        if (!FileUtils.isExistingFile(VIEWERS_FILEPATH)){
            FileUtils.createAnEmptyFile(VIEWERS_FILEPATH);
        }

        //Create a new bot object.
        setBot(new Bot(BOT_TWITCH_NICKNAME, CHANNEL_NAME, OPTIONS_FILEPATH, VIEWERS_FILEPATH));
        //Set bot's chat commands parse mode.
        bot.setParseChatCommands(parseChatCommands);

        //Retrieving an ip and port of the IRC server that we need to connect to.
        //As Twitch uses different servers for every user based on the popularity of their streams,
        //we have to use twitch api to retrieve ip and port.
        String ircIpPort = TwitchUtils.retrieveIrcIpPort(channelName);
        //They come in a string of following format "ip:port", so we split
        String[] partsOfIrcAddress = ircIpPort.split(":");
        //Set IRC ip address and port equal to those we recieved.
        IRC_ADDRESS = partsOfIrcAddress[0];
        PORT = Integer.parseInt(partsOfIrcAddress[1]);

        //Initialising the viewer list.
        Statistics.initializeViewersListFromFile(VIEWERS_FILEPATH);
    }

    /**
     * This method lets user tellUser the bot by replacing the default channel name(foreseer_) with
     * his own, if needed.
     */
    public void tellUser() {
        //Introduce ourself to user and tell instructions on changing the channel name(if needed).
        LogHandler.log("Hello!");
        LogHandler.log(String.format("Bot will now try to connect to the channel %s.", CHANNEL_NAME));
        //Tell information about further process.
        StringBuilder stringBuilder = new StringBuilder("Bot will ");
        if (!bot.isParseChatCommands()) {
            stringBuilder.append("not ");
        }
        stringBuilder.append("parse chat commands.");
        LogHandler.log(stringBuilder.toString());
    }

    @Override
    public void run() {
        tellUser();
        //Enabling/Disabling debug mode, the bot will write additional information in chat about connection if debug mode is ON
        bot.setVerbose(false);
        //Create a new console handler object.
        ConsoleHandler consoleHandler = new ConsoleHandler(VIEWERS_FILEPATH);
        //Initialise the console handler.
        bot.initialiseConsoleHandler(consoleHandler);
        //Create a new thread that console handler will run in.
        Thread consoleHandlerThread = new Thread(consoleHandler);
        //Start the console handler thread.
        consoleHandlerThread.start();

        try {
            //Connect to twitch IRC.
            bot.connect(IRC_ADDRESS, PORT, AUTH_TOKEN);
            //Try to join the specified channel.
            bot.joinChannel(CHANNEL_NAME);

            //I don't honestly remember why this pause is needed here
            //Probably to wait until IRC sends us the viewer list, although Twitch's IRC doesn't do that
            //I'll leave it here nonetheless
            Thread.sleep(5000);

            //This variable is used to check the amount of viewers
            int viewerCount = 0;
            //Main logic of the thread
            while (!Thread.currentThread().isInterrupted()) {
                //If we haven't checked the viewer count yet
                if (!checked) {
                    //Retrieve the amount of viewers via Twitch API
                    viewerCount = TwitchUtils.retrieveViewerCount(CHANNEL_NAME.substring(1));
                    //If it's above 100 we are disabling the greetviewers checkbox in the GUI.
                    if (viewerCount > 100) {
                        //Necessary to call through JavaFx thread, all the changes to GUI must be done
                        //in the same thread as GUI's controller.
                        Platform.runLater(() -> controller.greetViewersCheckBox.setDisable(true));
                    }
                    if (viewerCount != 0) {
                        checked = true;
                    }
                }


                //If there are less than 100 viewers on the stream, these functions will be working.
                //Although they're not really working properly due to Twitch's broken API :(
                if (viewerCount < 100 && viewerCount != 0) {
                    //Check for new viewers and greet them if such appeared.
                    bot.checkForNewViewers();
                    //Check for new followers on the stream
                    bot.checkLastFollower();
                }
                //Make a 10 seconds pause before checking for new viewers and followers again
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IrcException | IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            bot.close();
        }
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public String getCOMMANDS_SECTION() {
        return COMMANDS_SECTION;
    }

    public String getBLACKLIST_SECTION() {
        return BLACKLIST_SECTION;
    }

    public String getOPTIONS_FILEPATH() {
        return OPTIONS_FILEPATH;
    }

    public String getVIEWERS_FILEPATH() {
        return VIEWERS_FILEPATH;
    }

    public void close(){
        bot.close();
    }
}
