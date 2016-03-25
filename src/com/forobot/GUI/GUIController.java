package com.forobot.GUI;

import com.forobot.Bot.Functions.Cleaner;
import com.forobot.Bot.Functions.Statistics;
import com.forobot.Bot.Handlers.LogHandler;
import com.forobot.Bot.Operator;
import com.forobot.Bot.Handlers.SpamChecker;
import com.forobot.Utils.FileUtils;
import com.forobot.Utils.StringUtils;
import com.forobot.Utils.TwitchUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;

/**
 * Created by Foreseer on 16.03.2016.
 */
public class GUIController implements Initializable {

    private final String COMMANDS_SECTION = "ChatCommands";
    private final String BLACKLIST_SECTION = "Blacklist";
    @FXML
    private TextArea debugTextArea;
    @FXML
    private TextField debugTextField;
    @FXML
    private CheckBox parseChatCommandsCheckBox;
    @FXML
    private TextField channelNameTextField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button launchButton;
    @FXML
    private TextArea chatCommandsTextArea;
    @FXML
    private Tab chatCommandsTab;
    @FXML
    private Tab debugTab;
    @FXML
    private Tab blackListTab;
    @FXML
    private Tab optionsTab;
    @FXML
    private Tab statisticsTab;
    @FXML
    private TextField commandInitiatorTextField;
    @FXML
    private TextField commandResponseTextField;
    @FXML
    private CheckBox debugCheckBox;
    @FXML
    private TextField blacklistWordField;
    @FXML
    private TextArea blacklistWordArea;
    @FXML
    private CheckBox filterWordsCheckBox;
    @FXML
    public CheckBox greetViewersCheckBox;
    @FXML
    private CheckBox spellOutMessagesCheckBox;
    @FXML
    private Label mostActiveViewerLabel;
    @FXML
    private Label mostActiveViewerSessionLabel;
    @FXML
    private Label messagesAmountLabel;
    @FXML
    private CheckBox filterLinksCheckBox;
    @FXML
    private TextField spamDurationTextField;
    @FXML
    private ToggleButton filterSpamToggleButton;
    @FXML
    private ToggleButton filterWordsToggleButton;
    @FXML
    private ToggleButton filterLinksToggleButton;


    private OutputStream stream;
    private Operator operator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PrintStream printOutStream = new PrintStream(new ConsoleOutputHandler(debugTextArea));
        System.setOut(printOutStream);
        System.setErr(LogHandler.getExceptionPrintStream());

        TextFieldStreamer streamer = new TextFieldStreamer(debugTextField);
        debugTextField.addEventHandler(KeyEvent.KEY_PRESSED, streamer);
        System.setIn(streamer);

        Cleaner cleaner = new Cleaner(debugTextArea, this);
        Thread cleanerThread = new Thread(cleaner);
        cleanerThread.start();

        initializaInitialSettings();

    }

    public void clearDebugArea(){
        Platform.runLater(() -> debugTextArea.clear());

    }

    public void initializaInitialSettings(){
        String path = System.getProperty("user.dir");
        path = String.format("%s\\settings.ini", path);

        if (FileUtils.isExistingFile(path)){
            ArrayList<String> lines = FileUtils.readAllLinesFromFile(path);
            String[] parts = lines.get(0).split("=");
            channelNameTextField.setText(parts[1]);
        } else {
            ArrayList<String> lines = new ArrayList<>();
            lines.add("channel=foreseer_");

            FileUtils.createAnEmptyFile(path);
            FileUtils.writeAllLinesToTheFile(lines, path);
        }
    }

    public void saveChannelSettings(String channel){
        String path = System.getProperty("user.dir");
        path = String.format("%s\\settings.ini", path);

        ArrayList<String> lines = new ArrayList<>();
        lines.add(String.format("channel=%s", channel));
        FileUtils.writeAllLinesToTheFile(lines, path);
    }

    public void launchBot(ActionEvent actionEvent) {
        if (operator == null) {
            statusLabel.setText("Just a moment...");
            String channelName = channelNameTextField.getText();
            boolean parseChat = parseChatCommandsCheckBox.isSelected();
            if (!StringUtils.isAValidTwitchNickname(channelName)) {
                statusLabel.setText("Please, enter a valid channel name.");
                return;
            }
            if (!TwitchUtils.isAValidUser(channelName)) {
                statusLabel.setText("Such channel doesn't exist. Please, enter a valid channel name.");
                return;
            }
            channelName = channelName.toLowerCase();
            operator = new Operator(channelName, parseChat, this);
            Thread operatorThread = new Thread(operator);
            operatorThread.start();
            statusLabel.setText(String.format("Successfully launched the bot to the channel #%s", channelName));
            saveChannelSettings(channelName);

            launchButton.setDisable(true);

            debugTab.setDisable(false);
            chatCommandsTab.setDisable(false);
            blackListTab.setDisable(false);
            optionsTab.setDisable(false);
            statisticsTab.setDisable(false);

            refreshChatCommandsTextArea();
            refreshBlackListTextArea();
            initialiseStatisticsRefresher(mostActiveViewerLabel, mostActiveViewerSessionLabel);
        }
    }

    public Operator getOperator() {
        return operator;
    }

    public void exitApplication(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void changeParseMode(ActionEvent actionEvent) {
        if (operator != null) {
            boolean parseChat = parseChatCommandsCheckBox.isSelected();
            operator.getBot().setParseChatCommands(parseChat);
        }
    }

    public void refreshChatCommandsTextArea() {
        ArrayList<String> commands = FileUtils.readSectionFromFile(COMMANDS_SECTION, operator.getOPTIONS_FILEPATH());
        chatCommandsTextArea.clear();
        for (String command : commands) {
            chatCommandsTextArea.appendText(command + System.lineSeparator());
        }
    }

    public void refreshBlackListTextArea() {
        ArrayList<String> words = FileUtils.readSectionFromFile(BLACKLIST_SECTION, operator.getOPTIONS_FILEPATH());
        blacklistWordArea.clear();
        for (String word : words) {
            blacklistWordArea.appendText(word + System.lineSeparator());
        }
    }

    public void refreshChatCommandsTextFields() {
        commandInitiatorTextField.clear();
        commandInitiatorTextField.setPromptText("Initiator");

        commandResponseTextField.clear();
        commandResponseTextField.setPromptText("Response");
    }

    public void initialiseStatisticsRefresher(Label allTime, Label session){
        Statistics.Refresher refresher = new Statistics.Refresher(allTime, session, messagesAmountLabel);
        Thread thread = new Thread(refresher);
        thread.start();
    }

    public void setAllTimeText(String text){
        mostActiveViewerLabel.setText(text);
    }

    public void setSessionText(String text){
        mostActiveViewerSessionLabel.setText(text);
    }

    public void addChatCommand(ActionEvent actionEvent) {
        String initiator = commandInitiatorTextField.getText();
        String response = commandResponseTextField.getText();
        if (initiator.equals("")) {
            commandInitiatorTextField.clear();
            commandInitiatorTextField.setPromptText("Initiator field can not be empty!");
            return;
        }

        if (!StringUtils.isContainingOnlyDigitsAndLetter(initiator)) {
            commandInitiatorTextField.clear();
            commandInitiatorTextField.setPromptText("Initiator can only consist of alphabetic and numeric characters.");
            return;
        }

        initiator = String.format("!%s", initiator);
        if (response.equals("")) {
            commandResponseTextField.clear();
            commandResponseTextField.setPromptText("Response can not be empty!");
            return;
        }

        operator.getBot().getCommandHandler().addNewCommand(initiator, response);
        refreshChatCommandsTextArea();
        refreshChatCommandsTextFields();
    }

    public void removeChatCommand(ActionEvent actionEvent) {
        String initiator = commandInitiatorTextField.getText();
        if (initiator.equals("")) {
            commandInitiatorTextField.clear();
            commandInitiatorTextField.setPromptText("Initiator can not be empty!");
            return;
        }
        initiator = String.format("!%s", initiator);

        if (operator.getBot().getCommandHandler().isExistingCommand(initiator)) {
            operator.getBot().getCommandHandler().removeExistingCommand(initiator);
            refreshChatCommandsTextArea();
            refreshChatCommandsTextFields();
        } else {
            commandInitiatorTextField.clear();
            commandInitiatorTextField.setPromptText("Enter a valid initiator.");
        }
    }

    public void changeDebugState(ActionEvent actionEvent) {
        debugTextField.setDisable(!debugCheckBox.isSelected());
    }

    public void addBlacklistWord(ActionEvent actionEvent) {
        String word = blacklistWordField.getText();
        blacklistWordField.clear();

        SpamChecker spamChecker = operator.getBot().getSpamChecker();

        if (spamChecker.isExistingWord(word)) {
            blacklistWordField.clear();
            blacklistWordField.setPromptText(String.format("Word %s already exists in the blacklist.", word));
            return;
        }

        spamChecker.addNewProhibitedWord(word);
        refreshBlackListTextArea();


    }

    public void removeBlacklistWord(ActionEvent actionEvent) {
        String word = blacklistWordField.getText();
        blacklistWordField.clear();

        SpamChecker spamChecker = operator.getBot().getSpamChecker();
        if (!spamChecker.isExistingWord(word)) {
            blacklistWordField.clear();
            blacklistWordField.setPromptText(String.format("%s is not an existing word in the blacklist.", word));
            return;
        }

        spamChecker.removeExistingProhibitedWord(word);
        refreshBlackListTextArea();
    }

    public void changeFilteringState(ActionEvent actionEvent) {
        operator.getBot().getSpamChecker().setFilteringWords(filterWordsToggleButton.isSelected());
    }

    public void changeGreetMode(ActionEvent actionEvent) {
        operator.getBot().setGreetNewViewers(greetViewersCheckBox.isSelected());
    }

    public void changeSpellMode(ActionEvent actionEvent) {
        operator.getBot().setSpellOutMessages(spellOutMessagesCheckBox.isSelected());
    }

    public void changeFilteringSpamState(ActionEvent actionEvent) {
        operator.getBot().getSpamChecker().setFilteringSpam(filterSpamToggleButton.isSelected());
        filterWordsToggleButton.setDisable(!filterSpamToggleButton.isSelected());
        filterLinksToggleButton.setDisable(!filterSpamToggleButton.isSelected());
    }

    public void changeFilteringLinksState(ActionEvent actionEvent) {
        operator.getBot().getSpamChecker().setFilteringLinks(filterLinksToggleButton.isSelected());
    }

    public void changeDuration(ActionEvent actionEvent) {
        String value = spamDurationTextField.getText();
        if (!StringUtils.isContainingOnlyNumbers(value)){
            spamDurationTextField.setText("");
            spamDurationTextField.setPromptText("Only numbers are allowed here");
            return;
        }
        operator.getBot().setDurationOfBan(Integer.valueOf(value));
    }
}
