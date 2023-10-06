package org.example;


import org.example.util.WeatherUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot extends TelegramLongPollingBot {

    public Bot() {}

    // Write your Bot username here instead ...
    @Override
    public String getBotUsername() {
        return "...";
    }

    // Write your Bot token here instead ...
    @Override
    public String getBotToken() {
        return "...";
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        long userId = message.getFrom().getId();

        if (text.equals("/start")) {
            sendText(userId, "Hello, " + message.getFrom().getFirstName() + "! =)\nI am telegram weather bot. Write the place you'd like me to check out the weather for:");
        } else {
            ArrayList<String> listOfDescriptions = new ArrayList<>();
            ArrayList<String> listOfTemps = new ArrayList<>();
            ArrayList<String> listOfDates = new ArrayList<>();
            ArrayList<String> listOfTimes = new ArrayList<>();
            String responseBody = "", weatherIconCode;
            StringBuilder finalText = new StringBuilder();
            // Write your openWeatherMap Api key here instead ...
            String apiKey = "...";

            // Forming the request URL
            String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q=" + text.replace(" ", "%20") + "&appid=" + apiKey;

            // Creating an HTTP Client
            HttpClient httpClient = HttpClientBuilder.create().build();

            try {
                // Creating a GET request
                HttpGet httpGet = new HttpGet(apiUrl);

                // Executing the request
                HttpResponse response = httpClient.execute(httpGet);

                // Receiving a response as an HTTP entity
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    // Convert HTTP entity to string
                    responseBody = EntityUtils.toString(entity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(responseBody.contains("404")){
                sendText(userId, "I'm sorry =(, but I haven't found anything on your request. Would you like to know the weather for another city?");
            } else{
                Pattern patternToFindDescription = Pattern.compile("\"description\":\"(.*?)\",");
                Matcher matcherToFindDescription = patternToFindDescription.matcher(responseBody);
                while (matcherToFindDescription.find()) {
                    listOfDescriptions.add(matcherToFindDescription.group(1));
                }

                Pattern patternToFindTemperature = Pattern.compile("\"temp\":(.*?),");
                Matcher matcherToFindTemperature = patternToFindTemperature.matcher(responseBody);
                while (matcherToFindTemperature.find()) {
                    listOfTemps.add(String.valueOf((int)(Double.parseDouble(matcherToFindTemperature.group(1))-272.5)));
                }

                Pattern patternToFindDates = Pattern.compile("\"dt_txt\":\"(.*?)\\s");
                Matcher matcherToFindDates = patternToFindDates.matcher(responseBody);
                while (matcherToFindDates.find()) {
                    LocalDate date = LocalDate.parse(matcherToFindDates.group(1));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM, dd", Locale.ENGLISH);
                    String formattedDate = date.format(formatter);
                    listOfDates.add(formattedDate);
                }

                Pattern patternToFindTimes = Pattern.compile("\"dt_txt\":\"\\d{4}-\\d{2}-\\d{2}\\s(.*?):\\d{2}\"");
                Matcher matcherToFindTimes = patternToFindTimes.matcher(responseBody);
                while (matcherToFindTimes.find()) {
                    listOfTimes.add(matcherToFindTimes.group(1));
                }

                sendText(userId,"The temperature in " + text + " is following:\n");
                for (int i = 0; i < listOfDescriptions.size(); i++) {
                    finalText.append(listOfDates.get(i)).append(":\n");
                    String thisDay = listOfDates.get(i);
                    while(thisDay.equals(listOfDates.get(i))){
                        weatherIconCode = WeatherUtils.findTheRequiredIconCode(listOfDescriptions.get(i));
                        finalText.append("\t\t\t\t\t").append(listOfTimes.get(i)).append(": ").append(listOfTemps.get(i)).append("°С, ").append(listOfDescriptions.get(i)).append(weatherIconCode).append("\n");
                        i++;
                        if(i == listOfDates.size()) break;
                    }
                    i--;
                    sendText(userId, finalText.toString());
                    finalText = new StringBuilder();
                }
            }
        }
    }
}