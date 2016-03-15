package com.forobot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//TODO: Better spam filtering - "asdaasdasdasdad" shouldn't be passable (if same letters are repeated over the message and other letters don't appear)


/**
 * This class helps protecting user against spam
 */
public class SpamChecker {
    private final String BLACKLIST_PATH;
    private final String SECTION_NAME = "Blacklist";

    private ArrayList<String> blackList;

    public SpamChecker(String BLACKLIST_PATH) {
        this.BLACKLIST_PATH = BLACKLIST_PATH;
        blackList = new ArrayList<>();
        loadProhibitedWords();
    }

    /**
     * Checks whether the message is a spam message or not.
     *
     * @param message the message to be checked for spam
     * @return true if the message is spam
     */
    public boolean isSpam(String message) {
        if (containsConsecutiveRepeatingCharacters(message) || containsTooMuchCaps(message) || containsProhibitedWords(message)) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the message contains too many consecutive repeating characters.
     *
     * @param message the message to be checked
     * @return true if the message contains too many consecutive repeating characters
     */
    public boolean containsConsecutiveRepeatingCharacters(String message) {
        for (int i = 0; i <= message.length() - 4; i++) {
            int consecutiveLettersAmount = 0;

            for (int j = i + 1; j <= i + 3; j++) {
                if (message.charAt(j) == message.charAt(i)) {
                    consecutiveLettersAmount++;
                } else {
                    break; //I'm very sorry
                }
            }

            if (consecutiveLettersAmount == 3) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the message contains too many capital letters.
     *
     * @param message the message to be checked
     * @return true if the message contains too many capital letters.
     */
    public boolean containsTooMuchCaps(String message) {
        if (message.length() > 5 && isMostlyOfLetters(message)) {
            int amountOfCapitalLetters = 0;
            int amountOfSmallLetters = 0;
            for (int i = 0; i < message.length(); i++) {
                char currentChar = message.charAt(i);
                if (Character.isLetter(currentChar)) {
                    if (Character.isLowerCase(currentChar)) {
                        amountOfSmallLetters++;
                    } else {
                        amountOfCapitalLetters++;
                    }
                }
            }

            if (amountOfCapitalLetters >= amountOfSmallLetters) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Checks whether the message is mostly of letters.
     *
     * @param message the message to be checked
     * @return true if the message is mostly of letters.
     */
    public boolean isMostlyOfLetters(String message) {
        int letterAmount = 0;
        int numberAmount = 0;
        for (int i = 0; i < message.length(); i++) {
            char currentChar = message.charAt(i);
            if (Character.isLetter(currentChar)) {
                letterAmount++;
            } else if (Character.isDigit(currentChar)) {
                numberAmount++;
            }
        }

        if (numberAmount < letterAmount) {
            return true;
        } else {
            return false;
        }
    }

    public boolean containsProhibitedWords(String message) {
        for (String prohibitedWord : blackList) {
            if (message.contains(prohibitedWord)) {
                return true;
            }
        }
        return false;
    }

    private void loadProhibitedWords() {
        blackList = FileUtils.readSectionFromFile(SECTION_NAME, BLACKLIST_PATH);
    }

    public void addNewProhibitedWord(String word) {
        if (blackList.contains(word)) {
            System.out.println(String.format("Word %s already exists in the blacklist.", word));
        } else {
            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(BLACKLIST_PATH, true))) {
                FileUtils.addLineToTheSection(word, SECTION_NAME, BLACKLIST_PATH);
                blackList.add(word);
                System.out.println(String.format("Added new word \"%s\" to the blacklist.", word));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeExistingProhibitedWord(String word) {
        if (!blackList.contains(word)) {
            System.out.println(String.format("%s isn't an existing word in blacklist!", word));
        } else {
            blackList.remove(word);
            FileUtils.removeSpecificLineFromASection(word, SECTION_NAME, BLACKLIST_PATH);
            System.out.println(String.format("Word \"%s\" was removed successfully from the blacklist.", word));
        }
    }
}
