package com.forobot.Utils;

/**
 * Created by Foreseer on 17.03.2016.
 */
public class StringUtils {
    public static boolean isContainingOnlyDigitsAndLetter(String string) {
        for (int i = 0; i < string.length(); i++) {
            char currentChar = string.charAt(i);
            if (!Character.isLetter(currentChar) && !Character.isDigit(currentChar)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isContainingOnlyNumbers(String string) {
        for (int i = 0; i < string.length(); i++) {
            char currentChar = string.charAt(i);
            if (!Character.isDigit(currentChar)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAValidTwitchNickname(String string) {
        return string.matches("^[a-zA-Z0-9_]{4,25}$");
    }

    public static boolean isNumeric(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}
