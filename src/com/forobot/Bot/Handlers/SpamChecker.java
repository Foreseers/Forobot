package com.forobot.Bot.Handlers;

import com.forobot.Utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles all the anti-spam functionality.
 * Contains various methods that check if the message(or part of it) contains anything spam-related.
 */
public class SpamChecker {
    //Path to the blacklist file.
    private final String BLACKLIST_PATH;
    //Name of blacklist section in that file. Do not touch.
    private final String SECTION_NAME = "Blacklist";


    private boolean filteringWords;
    private boolean filteringLinks;

    private boolean filteringSpam;

    //The arraylist that contains all words that are not permitted for usage in the chat.
    private ArrayList<String> blackList;

    public SpamChecker(String BLACKLIST_PATH, boolean filteringWords) {
        this.BLACKLIST_PATH = BLACKLIST_PATH;
        blackList = new ArrayList<>();
        //Load the list of words that aren't permitted for usage in the chat.
        loadProhibitedWords();
        this.filteringWords = filteringWords;
        this.filteringSpam = false;
    }

    //Spam checks

    /**
     * Checks whether the message is a spam message or not. It will call other methods that are
     * returning true if the message doesn't pass any spam check. If any of them are returning true,
     * and we are currently filtering spam, this method returns true.
     *
     * @param message the message to be checked for spam
     * @return true if the message is spam
     */
    public boolean isSpam(String message) {
        if (filteringSpam) {
            if (containsConsecutiveRepeatingCharacters(message) || containsTooMuchCaps(message) || containsProhibitedWords(message)
                    || containsLinks(message) || containsSpamWords(message)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the message contains too many consecutive repeating characters. Currently it
     * checks if there are three of more repeating symbols, and return true if there are.
     *
     * @param message Message to be checked
     * @return Contains too many repeating symbols or not.
     */
    public boolean containsConsecutiveRepeatingCharacters(String message) {
        for (int i = 0; i <= message.length() - 4; i++) {
            int consecutiveLettersAmount = 0;

            for (int j = i + 1; j <= i + 3; j++) {
                if (message.charAt(j) == message.charAt(i)) {
                    consecutiveLettersAmount++;
                } else {
                    break;
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
     * Checks whether the message contains any words that are not permitted for usage. Simply uses
     * String.contains() for each entry in blackList ArrayList, returns true if String.contains()
     * returns true.
     *
     * @param message Message to be checked.
     * @return If message contains any prohibited words.
     */
    public boolean containsProhibitedWords(String message) {
        if (!filteringWords) {
            return false;
        }

        for (String prohibitedWord : blackList) {
            if (message.contains(prohibitedWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the message contains links.
     * Uses String.contains() to find out if the message contains "http://" or "htpps://" or "www.".
     *
     * @param message Message to be checked.
     * @return If message contains any links.
     */
    public boolean containsLinks(String message) {
        if (!filteringLinks) {
            return false;
        }

        if (message.contains("http://") || message.contains("https://") || message.contains("www.")) {
            return true;
        }
        return false;
    }

    public boolean containsSpamWords(String message) {
        List<String> words = Arrays.asList(message.split(" "));
        for (String word : words){
            Map<Character, Integer> lettersMap = new HashMap<>();
            for (int i = 0; i < word.length(); i++){
                char c = word.charAt(i);
                if (lettersMap.containsKey(c)){
                    int count = lettersMap.get(c);
                    lettersMap.put(c, ++count);
                } else {
                    lettersMap.put(c, 1);
                }
            }

            if (word.length() >= 6 && ((lettersMap.size() <= 2) || (!containsVowels(word)))){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the message is mostly of letters. Simply counts the amount of letters in the
     * message and compares it with the amount of numbers. Returns true if there are more letters,
     * false if more numbers.
     *
     * @param message Message to be checked.
     * @return If the message is mostly of letters.
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

    public boolean containsVowels(String word){
        ArrayList<Character> vowels = new ArrayList<>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');
        vowels.add('y');
        word = word.toLowerCase();
        for (int i = 0; i < word.length(); i++){
            if (vowels.contains(word.charAt(i))){
                return true;
            }
        }
        return false;
    }

     //Blacklist methods

    private void loadProhibitedWords() {
        blackList = FileUtils.readSectionFromFile(SECTION_NAME, BLACKLIST_PATH);
    }

    /**
     * Adds a new word to the blacklist.
     * Checks if such word is already in the blacklist, if not, adds a new word to blacklist ArrayList
     * and a new word to the file.
     *
     * @param word Word to be added.
     */
    public void addNewProhibitedWord(String word) {
        if (blackList.contains(word)) {
            LogHandler.log(String.format("Word %s already exists in the blacklist.", word));
        } else {
            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(BLACKLIST_PATH, true))) {
                FileUtils.addLineToTheSection(word, SECTION_NAME, BLACKLIST_PATH);
                blackList.add(word);
                LogHandler.log(String.format("Added new word \"%s\" to the blacklist.", word));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isExistingWord(String word) {
        return blackList.contains(word);
    }

    /**
     * Removes an existing word from the blacklist.
     * Checks if such word exists, if so, removes it from blacklist ArrayList and from the file.
     * @param word Word to be removed.
     */
    public void removeExistingProhibitedWord(String word) {
        if (!blackList.contains(word)) {
            LogHandler.log(String.format("%s isn't an existing word in blacklist!", word));
        } else {
            blackList.remove(word);
            FileUtils.removeSpecificLineFromASection(word, SECTION_NAME, BLACKLIST_PATH);
            LogHandler.log(String.format("Word \"%s\" was removed successfully from the blacklist.", word));
        }
    }

    //Various setters

    public void setFilteringWords(boolean filteringWords) {
        this.filteringWords = filteringWords;
        LogHandler.log(filteringWords ? "Blacklist filtering mode is ON" : "Blacklist filtering mode is OFF");
    }

    public void setFilteringSpam(boolean filteringSpam) {
        this.filteringSpam = filteringSpam;
        LogHandler.log(filteringSpam ? "Spam filtering mode is ON" : "Spam filtering mode is OFF");
    }

    public void setFilteringLinks(boolean filteringLinks) {
        this.filteringLinks = filteringLinks;
        LogHandler.log(filteringLinks ? "Links filtering mode is ON" : "Links filtering mode is OFF");
    }
}
