package com.forobot.Utils;

import com.forobot.Bot.Handlers.LogHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Foreseer on 17.03.2016.
 */
public class TwitchUtils {
    public static boolean isAValidUser(String username){
        String url = String.format("https://api.twitch.tv/kraken/channels/%s", username);
        JSONObject userInfo = null;
        try {
            userInfo = JSONParser.parseJsonFromUrl(url);
        } catch (JSONParser.ParsingException502 parsingException502) {
            LogHandler.log("Parsing exception 502/503.");
        }
        if (userInfo == null){
            return false;
        }
        return !userInfo.has("error");
    }

    public static String retrieveIrcIpPort(String username){
        String url = String.format("http://tmi.twitch.tv/servers?channel=%s", username);
        JSONObject jsonObject;
        try {
            jsonObject = JSONParser.parseJsonFromUrl(url);
            JSONArray serverArray = jsonObject.getJSONArray("servers");
            return serverArray.get(0).toString();
        } catch (JSONParser.ParsingException502 parsingException502) {
            parsingException502.printStackTrace();
        }
        return null;
    }

    public static int retrieveViewerCount(String username){
        if (isAValidUser(username)){
            String url = String.format("https://api.twitch.tv/kraken/streams/%s", username);
            try {
                JSONObject streamGenInfo = JSONParser.parseJsonFromUrl(url);
                JSONObject object = streamGenInfo.getJSONObject("stream");
                if (object == null){
                    return 0;
                }
                JSONObject streamInfo = streamGenInfo.getJSONObject("stream");
                return streamInfo.getInt("viewers");
            } catch (JSONParser.ParsingException502 parsingException502) {
                parsingException502.printStackTrace();
                return 0;
            } catch (JSONException exception){
                LogHandler.log("JSONException while parsing viewer count!");
                return 0;
            }
        }
        return 0;
    }

    public static ArrayList<String> retrieveEmotesList(){
        ArrayList<String> emotesList = new ArrayList<>();

        try {
            URL url = new URL("https://twitchemotes.com/filters/global");
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                if (line.contains("<br/>")) {
                    line = line.replace("<br/>", "");
                    emotesList.add(line);
                }
            }
            scanner.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emotesList;
    }
}
