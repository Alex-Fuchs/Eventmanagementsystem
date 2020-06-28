package de.unipassau.fim.projekt40.data_access_layer;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.IOException;

import java.util.Scanner;

/**
 * JSON Weather API for getting the weather of an event.
 */
public class JsonWeatherAPI
{
    /**
     * Gets the id of the city of the event.
     *
     * @param city city of the event.
     * @return id of the city.
     */
    public static String getWoeid(String city)
    {
        try {
            String inline = useAPI("https://www.metaweather.com/api/location/search/?query=" + city);
            if (!inline.equals("[]")) {
                String[] words = inline.split(",");
                String[] erg = words[2].split(":");
                return erg[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the weather of an event by {@code id}.
     *
     * @param id Id of the city of the event.
     * @param datum datum of the event.
     * @return weather as String.
     */
    public static String getWeather(String id, String datum) {
        String[] zahl = datum.split("-");
        datum = zahl[0] + "/" + zahl[1] + "/" + zahl[2];
        try {
            String inline = useAPI("https://www.metaweather.com/api/location/" + id + "/" + datum);
            if (!inline.equals("[]")) {
                String[] words = inline.split(",");
                String[] erg = words[1].split(":");
                return erg[1];
            } else {
                return "Kurzzeitige Störung";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends a get request to the weatherAPI.
     *
     * @param urlString String of the url.
     * @return answer of the API.
     */
    private static String useAPI(String urlString) throws IOException {
        String inline = "";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responsecode = conn.getResponseCode();
        if(responsecode == 200) {
            Scanner sc = new Scanner(url.openStream());
            while(sc.hasNext()) {
                inline += sc.nextLine();
            }
            sc.close();
            conn.disconnect();
            return inline;
        }
        conn.disconnect();
        return "[]";
    }
}