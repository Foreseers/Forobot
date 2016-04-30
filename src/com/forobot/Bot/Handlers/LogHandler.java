package com.forobot.Bot.Handlers;

import com.forobot.Utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * This class essentially handles all the logging functionality.
 * Will create new files "log.txt" and "exlog" in the app dir during initialisation.
 * Can log silently, i.e without telling user directly via "debug" window, and normally, i.e
 * writing out the logged message to user as well as logging it to the file.
 */

//TODO: Get rid of printing out logged messages with System.out, and write directly to the debug text are
//      To not create lags in the GUI while doing that, an implementation with thread updating the field once there are
//          5-10 more debug messages or every ten seconds would be sufficient.

public class LogHandler {
    //All the data that needs to be logged out is stored here.
    private static final ArrayList<String> LOG_DATA;
    //File path to log file.
    private static final String FILE_PATH;
    private static final String LOG_FILE_NAME = "log.txt";
    private static final String EXCEPTION_LOG_FILE_NAME = "exlog.txt";

    //Used to log exceptions via system.seterr().
    private static PrintStream exceptionPrintStream;

    static {
        File exceptionFile = new File(String.format("%s\\%s", System.getProperty("user.dir"), EXCEPTION_LOG_FILE_NAME));
        if (!exceptionFile.exists()){
            FileUtils.createAnEmptyFile(String.format("%s\\%s", System.getProperty("user.dir"), EXCEPTION_LOG_FILE_NAME));
        }
        try {
            exceptionPrintStream = new PrintStream(exceptionFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FILE_PATH = String.format("%s\\%s", System.getProperty("user.dir"), LOG_FILE_NAME);
        LOG_DATA = new ArrayList<>();
    }

    private LogHandler() {}

    /**
     * Logs the input by adding it to the log list and printing it out to the console.
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
     * Asynchronously adds log data to the LOG_DATA arraylist
     * @param data String that needs to be logged.
     */
    private static void asynchronouslyAddLogData(String data){
        new Thread(() -> {
            synchronized (LOG_DATA){
                LOG_DATA.add(data);
            }
        }).start();
    }

    public static PrintStream getExceptionPrintStream() {
        return exceptionPrintStream;
    }

    /**
     * Once called this method will write out all the logged data to the log file.
     */
    public static void close(){
        FileUtils.writeAllLinesToTheFile(LOG_DATA, FILE_PATH);
        exceptionPrintStream.close();
    }

}
