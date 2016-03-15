package com.forobot;

import java.util.ArrayList;

/**
 * Handles commands that users may send to the chat. Commands are sent following this syntax:
 * "!COMMAND". Commands consist of initiator(String) and response(String). Special commands: !list
 * lists all available commands.
 */
public class CommandHandler {
    private final String COMMANDS_FILE_PATH;
    private final String SECTION_NAME = "ChatCommands";
    ArrayList<Command> commands = new ArrayList<>();

    public CommandHandler(String commandsFilePath) {
        COMMANDS_FILE_PATH = commandsFilePath;
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
            if (message.equals("!list")) {
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

    public String getResponse(String command) {
        if (command.equals("!list")) {
            return getCommandsList();
        }
        for (Command existingCommand : commands) {
            if (existingCommand.getInitiator().equals(command)) {
                return existingCommand.getResponse();
            }
        }
        return "haven't found response for command";
    }

    public boolean isExistingCommand(String initiator) {
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
            if (isExistingCommand(initiator)) {
                System.out.println(String.format("Command \"%s\" already exists with a response \"%s\"", initiator, response));
            } else {
                FileUtils.addLineToTheSection(String.format("%s=%s", initiator, response), SECTION_NAME, COMMANDS_FILE_PATH);
                System.out.println(String.format("Successfully added new command \"%s\" with a response \"%s\".", initiator, response));
            }
        } else {
            System.out.println("Command's initiator must start with a \"!\" symbol.");
        }
    }

    public void removeExistingCommand(String initiator) {
        if (!isExistingCommand(initiator)) {
            System.out.println(String.format("%s isn't an existing command!", initiator));
        } else {
            for (Command command : commands) {
                if (command.getInitiator().equals(initiator)) {
                    commands.remove(command);
                }
                FileUtils.removeSpecificLineFromASection(initiator, SECTION_NAME, COMMANDS_FILE_PATH);
                System.out.println(String.format("Command with an initiator \"%s\" removed successfully.", initiator));
            }
        }
    }

    private String getCommandsList() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Command command : commands) {
            String initiator = command.getInitiator();
            stringBuilder.append(String.format("%s ; ", initiator));
        }
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
    }
}


