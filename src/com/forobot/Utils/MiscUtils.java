package com.forobot.Utils;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * This class isn't serving any purpose, really, just dumping absolutely random methods here that I
 * needed during testing AND methods that are still used, but are too minor to create new classes for.
 * Such as method randomWithRange, that, despite being used in the bot, is very small, doesn't belong to any specific
 * category, so I am leaving it here.
 *
 * Neither of this methods are used in the program, most likely, so no documentation will be provided.
 * Although used methods will perhaps be documented, there's no guarantee as in the case if they were really useful,
 * I would probably find place for them in other classes. :)
 */
public class MiscUtils {
    public static void FillFileWithRandomViewers(String filename, int count){
        ArrayList<String> randomLines = new ArrayList<>();
        for (int i = 1; i <= count; i++){
            String name = "viewer" + i;
            String amount = String.valueOf(randomWithRange(1, count));
            //randomLines.add(String.format("%s=%s", name, amount));
            randomLines.add(amount);
        }
        FileUtils.writeAllLinesToTheFile(randomLines, filename);
    }

    /**
     * Return a random number within range [min, max].
     * @param min lower bound of the range
     * @param max upper bound of the range
     * @return random number.
     */
    public static int randomWithRange(int min, int max)
    {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    public static ArrayList<Integer> strtoint(ArrayList<String> arrayList){
        ArrayList<Integer> integers = new ArrayList<>();
        for (String string : arrayList){
            integers.add(Integer.parseInt(string));
        }
        return integers;
    }

    public static ArrayList<String> inttostr(ArrayList<Integer> arrayList){
        ArrayList<String> strings = new ArrayList<>();
        for (Integer integer : arrayList){
            strings.add(String.valueOf(integer));
        }
        return strings;
    }

    public static ArrayList<String> treetolist(TreeSet<Integer> treeSet){
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer integer : treeSet.descendingSet()){
            arrayList.add(String.valueOf(integer));
        }
        return arrayList;
    }
}
