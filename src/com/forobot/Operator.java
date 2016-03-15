package com.forobot;

import org.jibble.pircbot.IrcException;

import java.io.IOException;
import java.util.Scanner;

//This is a general TO DO list.
//TODO: Request music from youtube (low priority, hard to implement)
//TODO: Polls
//TODO: Adding time onto a counter for marathons per each subscribe/donation.
//TODO: Custom API support (nightbot has it I guess?)

/**
 * This class operates all other classes and makes them work together.
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
    private final String IRC_ADDRESS = "irc.twitch.tv";
    //Twitch IRC port.
    private final int PORT = 6667;
    //Authentication token, used for authentication on twitch.
    private final String AUTH_TOKEN = "oauth:n4wixjncrxpigyi9c5qrzt2saqnq47";
    //Channel name that bot will connect to, can be changed after start of the program by user.
    private String CHANNEL_NAME = "#foreseer_";
    private Bot bot;

    public static void main(String[] args) throws Exception {
        //Make a new operator object. Operator object is a bot supervisor and will handle bot thread.
        Operator operator = new Operator();
        //Create a bot object.
        operator.setBot(new Bot(operator.BOT_TWITCH_NICKNAME, operator.CHANNEL_NAME));
        //Initialise the operator object. Initialisation allows user to specify the channel name to connect to.
        operator.initialise();

        //Make a new thread that operator object will run in.
        Thread operatorThread = new Thread(operator);

        //Start the thread.
        operatorThread.start();
    }

    /**
     * This method lets user initialise the bot by replacing the default channel name(foreseer_)
     * with his own, if needed.
     */
    public void initialise() {
        //Introduce ourself to user and tell instructions on changing the channel name(if needed).
        System.out.println("Hello!");
        System.out.println(String.format("Please, specify the channel name. By default, it's %s.", CHANNEL_NAME.substring(1)));
        System.out.println("To not specify anything, please type N.");

        //Initialise the scanner to parse the user input.
        Scanner scanner = new Scanner(System.in);

        //Parse the line that user entered in the console in response to request to specify channel name.
        String channelName = scanner.nextLine();
        //Check whether the parsed line equals to "N", if not, replace the channel name with specified by user.
        if (!channelName.equals("N")) {
            CHANNEL_NAME = String.format("#%s", channelName);
            bot.setChannelName(CHANNEL_NAME);
        }

        System.out.println("Should chat commands be parsed? Type \"Y/N\".");
        //Parse the line that user entered in the console in response to request to specify whether parse mode should be on or off.
        String parseCommandsInput = scanner.nextLine();

        switch (parseCommandsInput) {
            case ("Y"):
                bot.setParseChatCommands(true);
                break;
            case ("N"):
                bot.setParseChatCommands(false);
                break;
            default:
                System.out.println("You have typed wrong input. Chat parsing mode will be set to OFF.");
                bot.setParseChatCommands(false);
                break;
        }

        //Tell information about further process.
        System.out.println(String.format("Now will try to connect to channel \"%s\"", CHANNEL_NAME));
    }

    @Override
    public void run() {
        //Enabling/Disabling debug mode, the bot will write additional information in chat about connection if debug mode is ON
        bot.setVerbose(false);

        //Create a new console handler object.
        ConsoleHandler consoleHandler = new ConsoleHandler();
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
            Thread.sleep(5000);

            while (!Thread.currentThread().isInterrupted()) {
                //Check for new viewers and greet them if such appeared.
                bot.checkForNewViewers();
                //Check for new followers on the stream
                bot.checkLastFollower();
                try {
                    Thread.sleep(20000);
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

    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
