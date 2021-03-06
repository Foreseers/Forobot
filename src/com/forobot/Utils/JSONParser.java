package com.forobot.Utils;

import com.forobot.Bot.Handlers.LogHandler;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * This class parses JSON objects from URLs.
 */
public class JSONParser {
    /**
     * Parses a JSONObject from a URL.
     *
     * @param urlString URL to parse JSON from.
     * @return parsed JSONObject.
     */
    public static JSONObject parseJsonFromUrl(String urlString) throws ParsingException502 {
        JSONObject object = null;
        try {
            URL url = new URL(urlString);
            Scanner scanner = new Scanner(url.openStream());
            String parsedData = new String();
            while (scanner.hasNext()) {
                parsedData += scanner.nextLine();
            }
            scanner.close();

            object = new JSONObject(parsedData);

        } catch (MalformedURLException e) {
            LogHandler.log("Wrong URL to parse");
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("502") || errorMessage.contains("503")) {
                throw new ParsingException502();
            } else {
                LogHandler.log(e.getMessage());
                return null;
            }
        }
        return object;
    }

    /**
     * I don't actually know what this class is made for.
     * TODO: Get rid of this class or find its purpose.
     */
    public static class ParsingException502 extends Exception {

    }
}
