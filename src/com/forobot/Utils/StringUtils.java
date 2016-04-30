package com.forobot.Utils;

/**
 * Contains methods that are helping working with Strings, such as methods that check if string is consisting of only numbers,
 * checks if a string would be fit for a nickname etc.
 */
public class StringUtils {
    /**
     * Checking whether a string contains any symbols besides digits and letters.
     * Returns true if a string doesn't contain anything besides digits and letters.
     *
     * @param string String to check
     * @return       True if passed the test.
     */
    public static boolean isContainingOnlyDigitsAndLetter(String string) {
        for (int i = 0; i < string.length(); i++) {
            char currentChar = string.charAt(i);
            if (!Character.isLetter(currentChar) && !Character.isDigit(currentChar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checking whether a string contains only numbers.
     * Returns true if a string is numeric.
     *
     * @param string String to check
     * @return       True if passed the test.
     */
    public static boolean isContainingOnlyNumbers(String string) {
        for (int i = 0; i < string.length(); i++) {
            char currentChar = string.charAt(i);
            if (!Character.isDigit(currentChar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a string could be a valid username for Twitch.
     * It's only allowed to use numbers, alphabet letters and underscore in a twitch username.
     *
     * @param string String to check
     * @return       True if can be a valid username.
     */
    public static boolean isAValidTwitchNickname(String string) {
        return string.matches("^[a-zA-Z0-9_]{4,25}$");
    }

    /**
     * Checks whether a string contains only numbers.
     * @param string String to check
     * @return       True if numeric.
     */
    public static boolean isNumeric(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}
