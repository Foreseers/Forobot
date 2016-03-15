package com.forobot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A helper class that provides a functionality of parsing lines from options file, adding new lines
 * and removing existing ones.
 */
public class FileUtils {
    public static ArrayList<String> readSectionFromFile(String sectionName, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Specified file doesn't exist!");
            return null;
        }

        boolean finished = false;
        boolean started = false;

        ArrayList<String> lines = readAllLinesFromFile(filePath);
        if (!lines.contains(String.format("[Begin%s]", sectionName))) {
            System.out.println("Specified section doesn't exist in the file!");
            return null;
        }

        int i = 0;
        ArrayList<String> sectionLines = new ArrayList<>();

        while (!finished) {
            String line = lines.get(i);
            if (started) {
                if (line.equals(String.format("[End%s]", sectionName))) {
                    finished = true;
                } else {
                    sectionLines.add(line);
                }
            } else {
                if (line.equals(String.format("[Begin%s]", sectionName))) {
                    started = true;
                }
            }
            i++;
        }
        return sectionLines;
    }

    public static ArrayList<String> readAllLinesFromFile(String filePath) {
        ArrayList<String> lines = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Specified file doesn't exist!");
            return null;
        }

        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            while (fileReader.ready()) {
                lines.add(fileReader.readLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    public static void removeSpecificLineFromASection(String line, String sectionName, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Specified file doesn't exist!");
            return;
        }

        ArrayList<String> lines = readAllLinesFromFile(filePath);
        if (!lines.contains(String.format("[Begin%s]", sectionName))) {
            System.out.println("Specified section doesn't exist!");
            return;
        }

        boolean lineExists = false;
        for (String fileLine : lines) {
            if (fileLine.startsWith(line)) {
                lineExists = true;
            }
        }
        if (!lineExists) {
            System.out.println("Specified line doesn't exist!");
            return;
        }


        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(line)) {
                lines.remove(i);
                i--;
            }
        }

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath))) {
            for (String outputLine : lines) {
                fileWriter.write(outputLine);
                if (lines.indexOf(outputLine) != lines.size() - 1) {
                    fileWriter.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addLineToTheSection(String line, String sectionName, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Specified file doesn't exist!");
            return;
        }

        ArrayList<String> lines = readAllLinesFromFile(filePath);
        if (!lines.contains(String.format("[Begin%s]", sectionName))) {
            System.out.println("Specified section doesn't exist!");
            return;
        }

        int indexOfSectionBeginning = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).equals(String.format("[Begin%s]", sectionName))) {
                indexOfSectionBeginning = i;
                break;
            }
        }

        lines.add(indexOfSectionBeginning + 1, line);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String outputLine : lines) {
                writer.write(outputLine);
                if (lines.indexOf(outputLine) != lines.size() - 1) {
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
