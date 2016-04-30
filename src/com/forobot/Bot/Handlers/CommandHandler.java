package com.forobot.Bot.Handlers;

import com.forobot.Bot.Bot;
import com.forobot.Bot.Functions.Events.EventHandler;
import com.forobot.Bot.Functions.Statistics;
import com.forobot.Utils.FileUtils;
import com.forobot.Utils.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

//TODO: Raid handling. All functionality of raids currently should be working, only need to make it response on such command.

/**
 * Handles commands that users may send to the chat. Commands are sent following this syntax:
 * "!COMMAND". Commands consist of initiator(String) and response(String). Special commands: !list
 * lists all available commands.
 */
public class CommandHandler {
    private final String ADMIN_NAME;

    private final String COMMANDS_FILE_PATH;
    private final String SECTION_NAME = "ChatCommands";
    private final ArrayList<String> reservedCommands = new ArrayList<>();
    HashSet<Command> commands = new HashSet<>();

    public CommandHandler(String commandsFilePath, String adminName) {
        COMMANDS_FILE_PATH = commandsFilePath;
        ADMIN_NAME = adminName;

        reservedCommands.add("!roulette");
        reservedCommands.add("!money");
        reservedCommands.add("!list");
        reservedCommands.add("!botinfo");
        reservedCommands.add("!givemoney");
        reservedCommands.add("!raid");
        reservedCommands.add("!vote");

        loadCommands();
    }

    /**
     * This method is called once this class is instantiated, loads all the commands from the
     * commands file into the ArrayList. Commands are parsed line by line, all following the
     * "initiator=response" format.
     */
    private void loadCommands() {
        ArrayList<String> commandLines = FileUtils.readSectionFromFile(SECTION_NAME, COMMANDS_FILE_PATH);
        for (String commandLine : commandLines) {
            String[] partsOfCommand = commandLine.split("=");
            String initiator = partsOfCommand[0];
            String response = partsOfCommand[1];
            Command command = new Command(initiator, response);
            commands.add(command);
        }
    }

    public boolean isCommand(String message) {
        if (message.startsWith("!")) {
            message = message.toLowerCase();
            if (reservedCommands.contains(message)) {
                return true;
            }
            for (Command existingCommand : commands) {
                if (existingCommand.getInitiator().equals(message)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void respondCommand(Bot bot, String sender, String command) {
        command = command.toLowerCase();
        if (command.equals("!list")) {
            bot.sendMessage("Stream-specific commands: " + getCommandsList());
            bot.sendMessage("Bot commands: " + getReservedCommandsList());
            return;
        }

        if (command.startsWith("!roulette")) {
            String[] parts = command.split(" ");
            if (parts.length == 1) {
                bot.sendMessage(EventHandler.initiateNewEvent(bot, Statistics.getViewer(sender), 0, EventHandler.EventType.EVENT_ROULETTE));
                return;
            } else {
                if (StringUtils.isNumeric(parts[1])) {
                    if (Integer.parseInt(parts[1]) == 0){
                        bot.sendMessage("Please, enter a valid number for roulette.");
                        return;
                    }
                    if (Statistics.hasEnoughMoney(sender, Integer.parseInt(parts[1]))) {
                        bot.sendMessage(EventHandler.initiateNewEvent(bot, Statistics.getViewer(sender), Integer.parseInt(parts[1]), EventHandler.EventType.EVENT_ROULETTE));
                        return;
                    } else {
                        bot.sendMessage(String.format("You don't have enough money for that, %s!", sender));
                        return;
                    }
                } else {
                    bot.sendMessage("Please, enter a valid number for roulette.");
                    return;
                }

            }
        }

        if (command.equals("!money")) {
            int money = Statistics.getViewer(sender).getMoneyAmount();
            bot.sendMessage(String.format("You have %d coins, %s", money, sender));
            return;
        }

        if (command.equals("!botinfo")){
            bot.sendMessage("Forobot is a general purpose Twitch bot written by Konstantin \"Foreseer\". If you have" +
                    "any suggestions, please do not hesitate to write to me at skype \"dazem11\".");
            return;
        }

        if (command.startsWith("!givemoney")){
            String[] parts = command.split(" ");
            if (parts.length != 3){
                bot.sendMessage("Invalid command. Please type in in format \"!givemoney receiver amount\"");
                return;
            }
            if (!StringUtils.isNumeric(parts[2])){
                bot.sendMessage("Amount should be numeric!");
                return;
            }
            if (!Statistics.isAnActiveViewer(parts[1])){
                bot.sendMessage("Receiver doesn't exist in the base!");
                return;
            }
            int amount = Integer.parseInt(parts[2]);
            if (amount <= 0){
                bot.sendMessage("Amount should be more than 0!");
                return;
            }
            String receiver = parts[1].toLowerCase();
            if (sender.equals(ADMIN_NAME)){
                Statistics.increaseCoinsAmount(receiver, amount);
                bot.sendMessage(String.format("Successfully gave %s %d coins!", receiver, amount));
                return;
            } else {
                if (!Statistics.hasEnoughMoney(sender, amount)){
                    bot.sendMessage("You don't have enough coins for that!");
                    return;
                } else {
                    Statistics.getViewer(sender).setMoneyAmount(Statistics.getViewer(sender).getMoneyAmount() - amount);
                    Statistics.increaseCoinsAmount(receiver, amount);
                    bot.sendMessage(String.format("Successfully gave %s %d coins!", receiver, amount));
                    return;
                }
            }
        }

        if (command.startsWith("!raid")){
            String[] parts = command.split(" ");
            if (!parts[0].equals("!raid")){
                System.out.println("Invalid command.");
                return;
            }
            if (!EventHandler.isThereAnActiveRaid()){
                bot.sendMessage("There is no active raid started by the streamer.");
                return;
            }
            if (parts.length > 2){
                bot.sendMessage("Invalid command. To participate in the raid, send a message following such format: \"!raid amount\" or simply\"!raid\"" +
                        "to participate with all your coins");
                return;
            }
            int amount;
            if (parts.length != 1) {
                if (!StringUtils.isNumeric(parts[1])) {
                    bot.sendMessage("Amount should be numeric.");
                    return;
                }
                amount = Integer.parseInt(parts[1]);
                if (amount <= 0) {
                    bot.sendMessage("Amount should be more than 0.");
                    return;
                }
                if (!Statistics.hasEnoughMoney(sender, amount)) {
                    bot.sendMessage("You don't have enough money for that!");
                    return;
                }
            } else {
                amount = Statistics.getViewer(sender).getMoneyAmount();
            }
            Statistics.decreaseCoinsAmount(sender, amount);
            EventHandler.initiateNewEvent(bot, Statistics.getViewer(sender), amount, EventHandler.EventType.EVENT_RAID);
            return;
        }

        if (command.startsWith("!vote")){
            String[] parts = command.split(" ");
            if (parts.length != 2 || !parts[0].equals("!vote") || !StringUtils.isNumeric(parts[1])){
                bot.sendMessage("Send a vote command in following format: \"!vote optionnumber\", where option number is numeric");
                return;
            }
            if (!EventHandler.isThereAnActivePoll()){
                bot.sendMessage("There is no active poll right now!");
                return;
            }
            EventHandler.initiateNewEvent(bot, Statistics.getViewer(sender), Integer.parseInt(parts[1]), EventHandler.EventType.EVENT_POLL);
            return;
        }

        for (Command existingCommand : commands) {
            if (existingCommand.getInitiator().equals(command)) {
                bot.sendMessage(existingCommand.getResponse());
                return;
            }
        }
        bot.sendMessage("haven't found response for command");
    }

    public boolean isExistingCommand(String initiator) {
        initiator.toLowerCase();
        if (reservedCommands.contains(initiator)) {
            return true;
        }

        for (Command command : commands) {
            if (command.getInitiator().equals(initiator)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds new command to the commands file and makes it available for handling immidiately.
     *
     * @param initiator of a command
     * @param response  to a command
     */
    public void addNewCommand(String initiator, String response) {
        if (initiator.startsWith("!")) {
            if (reservedCommands.contains(initiator)) {
                LogHandler.log(String.format("Initiator %s is reserved for bot functionality command. Use another one.", initiator));
                return;
            }

            if (isExistingCommand(initiator)) {
                LogHandler.log(String.format("Command \"%s\" already exists with a response \"%s\"", initiator, response));
            } else {
                initiator = initiator.toLowerCase();
                FileUtils.addLineToTheSection(String.format("%s=%s", initiator, response), SECTION_NAME, COMMANDS_FILE_PATH);
                commands.add(new Command(initiator, response));
                LogHandler.log(String.format("Successfully added new command \"%s\" with a response \"%s\".", initiator, response));
            }
        } else {
            LogHandler.log("Command's initiator must start with a \"!\" symbol.");
        }
    }

    public void removeExistingCommand(String initiator) {
        initiator = initiator.toLowerCase();
        if (!isExistingCommand(initiator)) {
            LogHandler.log(String.format("%s isn't an existing command!", initiator));
        } else {
            for (Iterator<Command> iterator = commands.iterator(); iterator.hasNext(); ) {
                Command command = iterator.next();
                if (command.getInitiator().equals(initiator)) {
                    iterator.remove();
                }
                FileUtils.removeSpecificLineFromASection(initiator, SECTION_NAME, COMMANDS_FILE_PATH);
                LogHandler.log(String.format("Command with an initiator \"%s\" removed successfully.", initiator));
            }
        }
    }

    private String getCommandsList() {
        if (commands.size() == 0){
            return "no commands;";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Command command : commands) {
            String initiator = command.getInitiator();
            stringBuilder.append(initiator + ", ");
        }
        stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private String getReservedCommandsList(){
        StringBuilder stringBuilder = new StringBuilder();
        for (String command : reservedCommands){
            stringBuilder.append(command + ", ");
        }
        stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private class Command {
        private String initiator;
        private String response;

        public Command(String initiator, String response) {
            this.initiator = initiator;
            this.response = response;
        }

        public String getInitiator() {
            return initiator;
        }

        public void setInitiator(String initiator) {
            this.initiator = initiator;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        @Override
        public int hashCode() {
            int result = 31;
            result *= initiator.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof Command)) {
                return false;
            }

            Command command = (Command) obj;
            return command.getInitiator().equals(this.getInitiator());
        }
    }
}


