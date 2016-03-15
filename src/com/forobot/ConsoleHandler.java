package com.forobot;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class handles and responds on console commands. Dependent on Bot, CommandHandler and
 * SpeechSynthesizer classes. Sends commands directly to objects of those classes to control them,
 * if needed.
 */

/*
 *  Available console commands:
 * !add !initiator response | adds a new chat command available to request via chat
 * !remove !initiator       | removes an existing chat command !list | lists all console commands
 * !parse true/false        | sets chat parsing mode to true of false(default: false)
 * !spamAdd word            | adds a new word to the blacklist
 * !spamRemove word         | removes an existing word from the blacklist
 * !exit                    | terminates the app execution
 */

public class ConsoleHandler implements Runnable {
    //Filepath to the file with console commands. Not used anymore.
    //private final String CONSOLE_COMMANDS_FILEPATH = "d:\\consolecommands.txt";

    //Objects to operate if requested by console commands.
    private Bot bot;
    private CommandHandler commandHandler;
    private SpeechSynthesizer speechSynthesizer;

    //A list containing all available commands.
    private ArrayList<String> availableCommands;

    public ConsoleHandler() {
        availableCommands = new ArrayList<>();
        //Load all the console commands into the ArrayList.
        loadCommands();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            //Create a scanner object to read input from the console.
            Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
            //If user wrote something in the console, read it and respond on it if it's a valid command.
            if (scanner.hasNext()) {
                //Parsing the line.
                String input = scanner.next();
                //Checking whether it's a valid command or not.
                if (isConsoleCommand(input)) {
                    //If it's a valid command, respond on it.
                    handleCommand(input);
                } else {
                    //If it's not a valid command, tell that to user.
                    System.out.println("Invalid console command!");
                }
            }
        }
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public void setCommandHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public void setSpeechSynthesizer(SpeechSynthesizer speechSynthesizer) {
        this.speechSynthesizer = speechSynthesizer;
    }

    /**
     * Parses commands from the console commands file and adds them to the availableCommands
     * ArrayList.
     */
    private void loadCommands() {
        //Add all the commands to available commands ArrayList.
        availableCommands.add("!add !initiator response | adds a new chat command available to request via chat");
        availableCommands.add("!remove !initiator       | removes an existing chat command");
        availableCommands.add("!list                    | lists all console commands");
        availableCommands.add("!parse true/false        | sets chat parsing mode to true of false(default: false)");
        availableCommands.add("!spamAdd word            | adds a new word to the blacklist");
        availableCommands.add("!spamRemove word         | removes an existing word from the blacklist");
        availableCommands.add("!exit                    | terminates the app execution");
    }

    /**
     * Checks if a string is a valid console command.
     *
     * @param consoleCommand String to check for validity.
     * @return true is param string is a valid console command.
     */
    public boolean isConsoleCommand(String consoleCommand) {

        String[] partsOfCommand = consoleCommand.split(" ");
        for (String existingCommand : availableCommands) {
            String initiator = existingCommand.split(" ")[0];
            if (initiator.equals(partsOfCommand[0])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Responds on the command.
     *
     * @param consoleCommand Command to respond.
     */
    public void handleCommand(String consoleCommand) {
        //Split command string by space regex
        String[] partsOfCommand = consoleCommand.split(" ");
        String consoleCommandInitiator = partsOfCommand[0];
        String initiator;
        String response;

        switch (consoleCommandInitiator) {
            case ("!add"):
                initiator = partsOfCommand[1];
                response = partsOfCommand[2];
                commandHandler.addNewCommand(initiator, response);
                break;
            case ("!remove"):
                initiator = partsOfCommand[1];
                commandHandler.removeExistingCommand(initiator);
                break;
            case ("!list"):
                listCommands();
                break;
            case ("!parse"):
                boolean parseBool = Boolean.parseBoolean(partsOfCommand[1]);
                bot.setParseChatCommands(parseBool);
                break;
            case ("!spamAdd"): {
                String word = partsOfCommand[1];
                bot.addNewProhibitedWord(word);
                break;
            }
            case ("!spamRemove"): {
                String word = partsOfCommand[1];
                bot.removeExistingProhibitedWord(word);
                break;
            }
            case ("!exit"):
                System.exit(0);
                break;

        }
    }

    private void listCommands() {
        for (String command : availableCommands) {
            System.out.println(command);
        }
    }
}
