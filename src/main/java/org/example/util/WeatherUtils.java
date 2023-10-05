package org.example.util;

import java.util.HashMap;
import java.util.Map;

public class WeatherUtils {
    public final static Map<String, String> weatherIconsCodes = new HashMap<>();

    static {
        weatherIconsCodes.put("clear", "☀");
        weatherIconsCodes.put("rain", "☔");
        weatherIconsCodes.put("snow", "❄");
        weatherIconsCodes.put("clouds", "☁");
        weatherIconsCodes.put("fog", "\uD83C\uDF2B️");
    }

    public static String findTheRequiredIconCode(String weather){
        weather = weather.toLowerCase();
        for (Map.Entry<String, String> entry : weatherIconsCodes.entrySet()) {
            if (weather.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "";
    }
}