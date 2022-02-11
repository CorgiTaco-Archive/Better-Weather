package corgitaco.betterweather.common.weather.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.common.weather.WeatherContext;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class WeatherConfigSerializers {
    public static void handleEventConfigs(boolean isClient, Map<String, WeatherEvent> weatherEvents, Path weatherEventsConfigPath) {
        File eventsDirectory = weatherEventsConfigPath.toFile();
        if (!eventsDirectory.exists()) {
            createDefaultEventConfigs(weatherEventsConfigPath);
        }

        File[] files = eventsDirectory.listFiles();

        if (files.length == 0) {
            createDefaultEventConfigs(weatherEventsConfigPath);
        }

        if (isClient) {
            addSettingsIfMissing(weatherEvents, weatherEventsConfigPath);
        }

        iterateAndReadConfiguredEvents(files, isClient, weatherEventsConfigPath, weatherEvents);
    }

    public static void iterateAndReadConfiguredEvents(File[] files, boolean isClient, Path weatherEventsConfigPath, Map<String, WeatherEvent> weatherEvents) {
        for (File configFile : files) {
            String absolutePath = configFile.getAbsolutePath();
//            if (absolutePath.endsWith(".toml")) {
//                readToml(isClient, configFile);

//            } else if (absolutePath.endsWith(".json")) {
            readJson(isClient, configFile, weatherEventsConfigPath, weatherEvents);
//            }
        }
    }


    public static void createDefaultEventConfigs(Path weatherEventsConfigPath) {
        for (Map.Entry<ResourceLocation, WeatherEvent> entry : BetterWeatherRegistry.DEFAULT_EVENTS.entrySet()) {
            ResourceLocation location = entry.getKey();
            WeatherEvent event = entry.getValue();
            Optional<RegistryKey<Codec<? extends WeatherEvent>>> optionalKey = BetterWeatherRegistry.WEATHER_EVENT.getResourceKey(event.codec());

            if (optionalKey.isPresent()) {
                createJsonEventConfig(event, location.toString(), weatherEventsConfigPath);
            } else {
                throw new IllegalStateException("Weather Event Key for codec not there when requested: " + event.getClass().getSimpleName());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void addSettingsIfMissing(Map<String, WeatherEvent> weatherEvents, Path weatherEventsConfigPath) {
        for (Map.Entry<String, WeatherEvent> entry : weatherEvents.entrySet()) {
            WeatherEvent event = entry.getValue();
            String key = entry.getKey();
            File tomlFile = weatherEventsConfigPath.resolve(key + ".toml").toFile();
            File jsonFile = weatherEventsConfigPath.resolve(key + ".json").toFile();
            Optional<RegistryKey<Codec<? extends WeatherEvent>>> optionalKey = BetterWeatherRegistry.WEATHER_EVENT.getResourceKey(event.codec());

            if (optionalKey.isPresent()) {
                if (!tomlFile.exists() && !jsonFile.exists()) {
                    createJsonEventConfig(event, key, weatherEventsConfigPath);
                }
            } else {
                throw new IllegalStateException("Weather Event Key for codec not there when requested: " + event.getClass().getSimpleName());
            }
        }
    }


    public static void createJsonEventConfig(WeatherEvent weatherEvent, String weatherEventID, Path weatherEventsConfigPath) {
        Path configFile = weatherEventsConfigPath.resolve(weatherEventID.replace(":", "-") + ".json");
        JsonElement jsonElement = WeatherEvent.CODEC.encodeStart(JsonOps.INSTANCE, weatherEvent).result().get();

        try {
            Files.createDirectories(configFile.getParent());
            Files.write(configFile, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jsonElement).getBytes());
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }


    public static void readJson(boolean isClient, File configFile, Path weatherEventsConfigPath, Map<String, WeatherEvent> weatherEvents) {
        try {
            String noTypeFileName = configFile.getName().replace(".json", "");
            String name = noTypeFileName.toLowerCase();
            WeatherEvent decodedValue = WeatherEvent.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(new FileReader(configFile))).resultOrPartial(BetterWeather.LOGGER::error).get().getFirst().setKey(name);

            // We need to recreate the json each time to ensure we're taking into account any config fixing.
            createJsonEventConfig(decodedValue, noTypeFileName, weatherEventsConfigPath);

            if (isClient) {
                if (weatherEvents.containsKey(name)) {
                    WeatherEvent weatherEvent = weatherEvents.get(name);
                    weatherEvent.setClientSettings(decodedValue.getClientSettings());
                    weatherEvent.setClient(weatherEvent.getClientSettings().createClientSettings(), configFile.getAbsolutePath());
                }
            } else {
                weatherEvents.put(name, decodedValue);
            }
        } catch (FileNotFoundException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }

    public static WeatherContext.WeatherTimeSettings readOrCreateConfigJson(File configFile) {
        if (!configFile.exists()) {
            try {
                Path path = configFile.toPath();
                Files.createDirectories(path.getParent());
                JsonElement jsonElement = WeatherContext.WeatherTimeSettings.CODEC.encodeStart(JsonOps.INSTANCE, WeatherContext.WeatherTimeSettings.DEFAULT).result().get();
                Files.write(path, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jsonElement).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return WeatherContext.WeatherTimeSettings.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(new FileReader(configFile))).result().orElseThrow(RuntimeException::new).getFirst();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
