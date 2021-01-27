package corgitaco.betterweather.config.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WeatherEventControllerConfig {


    public static void handleConfig(Path path) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();

        final File CONFIG_FILE = new File(String.valueOf(path));

        if (!CONFIG_FILE.exists()) {
            createJson(path);
        }
        try (Reader reader = new FileReader(path.toString())) {
            HashMap<String, Double> hashMap = gson.fromJson(reader, HashMap.class);
            if (hashMap != null) {
                HashMap<BetterWeatherID, Double> new_weather_event_controller = new HashMap<>();
                hashMap.forEach((string, chance) -> new_weather_event_controller.put(new BetterWeatherID(string), chance));

                WeatherEventSystem.WEATHER_EVENT_CONTROLLER = new_weather_event_controller;
            } else
                BetterWeather.LOGGER.info(path.getFileName().toString() + " failed to load!");

        } catch (IOException e) {
            BetterWeather.LOGGER.error(path.getFileName().toString() + " could not be read");
        }
    }


    public static void createJson(Path path) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();

        String jsonString = gson.toJson(WeatherEventSystem.WEATHER_EVENT_CONTROLLER);

        try {
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-biomes.json could not be created");
        }
    }

}
