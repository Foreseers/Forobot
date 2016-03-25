package com.forobot.Bot.Handlers;

import com.forobot.Utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * This class essentially handles all the logging functionality.
 * Will create new files "log.txt" and "exlog" in the app dir during initialisation.
 */
public class LogHandler {
    //All the data that needs to be logged out is stored here.
    private static ArrayList<String> logData;
    //File path to log file.
    private static String filePath;
    private static final String LOG_FILE_NAME = "log.txt";
    private static final String EXCEPTION_LOG_FILE_NAME = "exlog.txt";

    //Used to log exceptions via system.seterr.
    private static PrintStream exceptionPrintStream;

    static {
        filePath = System.getProperty("user.dir");

        File exceptionFile = new File(String.format("%s\\%s", filePath, EXCEPTION_LOG_FILE_NAME));
        if (!exceptionFile.exists()){
            FileUtils.createAnEmptyFile(String.format("%s\\%s", filePath, EXCEPTION_LOG_FILE_NAME));
        }
        try {
            exceptionPrintStream = new PrintStream(exceptionFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        filePath = String.format("%s\\%s", filePath, LOG_FILE_NAME);
        logData = new ArrayList<>();
    }

    private LogHandler() {}

    /**
     * Logs the input by adding it to the log list and printing it out to the console
     * @param message String that needs to be logged.
     */
    public static void log(String message){
        asynchronouslyAddLogData(message);
        System.out.println(message);
    }

    /**
     * Logs the input silently, without printing it out to the console.
     * @param message String that needs to be logged.
     */
    public static void logSilently(String message){
        asynchronouslyAddLogData(message);
    }

    /**
     * Asynchronously adds log data to the logData arraylist
     * @param data String that needs to be logged.
     */
    private static void asynchronouslyAddLogData(String data){
        new Thread(() -> {
            synchronized (logData){
                logData.add(data);
            }
        }).start();
    }

    public static PrintStream getExceptionPrintStream() {
        return exceptionPrintStream;
    }

    /**
     * Once called this method will write out all the logged data to the log file
     */
    public static void close(){
        FileUtils.writeAllLinesToTheFile(logData, filePath);
        exceptionPrintStream.close();
    }

}
