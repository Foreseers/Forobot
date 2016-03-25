package com.forobot.Utils;

import com.forobot.Bot.Handlers.LogHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * A helper class that provides a functionality of parsing lines from options file, adding new lines
 * and removing existing ones.
 */
public class FileUtils {
    public static ArrayList<String> readSectionFromFile(String sectionName, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            LogHandler.log("Specified file doesn't exist!");
            return null;
        }

        boolean finished = false;
        boolean started = false;

        ArrayList<String> lines = readAllLinesFromFile(filePath);
        if (!lines.contains(String.format("[Begin%s]", sectionName))) {
            LogHandler.log("Specified section doesn't exist in the file!");
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
            LogHandler.log("Specified file doesn't exist!");
            return null;
        }

        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            while (fileReader.ready()) {
                lines.add(fileReader.readLine());
            }
            fileReader.close();
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
            LogHandler.log("Specified file doesn't exist!");
            return;
        }

        ArrayList<String> lines = readAllLinesFromFile(filePath);
        if (!lines.contains(String.format("[Begin%s]", sectionName))) {
            LogHandler.log("Specified section doesn't exist!");
            return;
        }

        boolean lineExists = false;
        for (String fileLine : lines) {
            if (fileLine.startsWith(line)) {
                lineExists = true;
            }
        }
        if (!lineExists) {
            LogHandler.log("Specified line doesn't exist!");
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
            LogHandler.log("Specified file doesn't exist!");
            return;
        }

        ArrayList<String> lines = readAllLinesFromFile(filePath);
        if (!lines.contains(String.format("[Begin%s]", sectionName))) {
            LogHandler.log("Specified section doesn't exist!");
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

    public static void writeAllLinesToTheFile(ArrayList<String> lines, String filePath){
        File file = new File(filePath);
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file))){
            for (String line : lines){
                fileWriter.write(line + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object deserialiseFromFile(String fileName){
        Object object = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            object = objectInputStream.readObject();

            fileInputStream.close();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static void serialiseToFile(Object object, String fileName){
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);

            fileOutputStream.close();
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            LogHandler.log("Haven't found such file.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean isExistingFile(String filename){
        File file = new File(filename);
        return file.exists();
    }

    public static void createASectionsFile(String filepath, String... sections){
        File file = new File(filepath);
        if (file.exists()){
            LogHandler.log("Sections file already exists!");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < sections.length; i++) {
                String name = sections[i];
                StringBuilder output = new StringBuilder("[Begin");
                output.append(name);
                output.append("]");
                output.append(System.lineSeparator());

                writer.write(output.toString());

                output = new StringBuilder("[End");
                output.append(name);
                output.append("]");
                if (i != sections.length - 1){
                    output.append(System.lineSeparator());
                }

                writer.write(output.toString());
            }
            LogHandler.log("Created a new sections file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createAnEmptyFile(String filepath){
        File file = new File(filepath);
        if (file.exists()){
            LogHandler.log("File already exists!");
            return;
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            LogHandler.log("Was an error creating a new empty file.");
        }
    }

    public static void createAFolder(String path, String folderName){
        String newDir = String.format("%s%s", path, folderName);
        new File(newDir).mkdir();
    }
}
