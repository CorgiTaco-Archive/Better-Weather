package corgitaco.betterweather.weather;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventContext;
import corgitaco.betterweather.api.weather.WeatherEventSettings;
import corgitaco.betterweather.data.storage.WeatherEventSavedData;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BWWeatherEventContext implements WeatherEventContext {
    public static final String CONFIG_NAME = "weather-settings.toml";

    public static final Codec<BWWeatherEventContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.STRING.fieldOf("currentEvent").forGetter((weatherEventContext) -> {
            return weatherEventContext.currentEvent.getName();
        }), ResourceLocation.CODEC.fieldOf("worldID").forGetter((weatherEventContext) -> {
            return weatherEventContext.worldID;
        }), Codec.unboundedMap(Codec.STRING, WeatherEvent.CODEC).fieldOf("weather_events").forGetter((weatherEventContext) -> {
            return weatherEventContext.weatherEvents;
        })).apply(builder, BWWeatherEventContext::new);
    });

    private final Map<String, WeatherEvent> weatherEvents = new HashMap<>();
    private final ResourceLocation worldID;
    private final Registry<Biome> biomeRegistry;
    private final Path weatherConfigPath;
    private final Path weatherEventsConfigPath;
    private final File weatherConfigFile;

    private WeatherEvent currentEvent;

    //Packet Constructor
    public BWWeatherEventContext(String currentEvent, ResourceLocation worldID, Map<String, WeatherEvent> weatherEvents) {
        this(currentEvent, worldID, null, weatherEvents);
    }

    //Server world constructor
    public BWWeatherEventContext(WeatherEventSavedData weatherEventSavedData, RegistryKey<World> worldID, Registry<Biome> biomeRegistry) {
        this(weatherEventSavedData.getEvent(), worldID.getLocation(), biomeRegistry, null);
    }

    public BWWeatherEventContext(String currentEvent, ResourceLocation worldID, @Nullable Registry<Biome> biomeRegistry, @Nullable Map<String, WeatherEvent> weatherEvents) {
        this.worldID = worldID;
        this.biomeRegistry = biomeRegistry;
        this.weatherConfigPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("weather");
        this.weatherEventsConfigPath = this.weatherConfigPath.resolve("events");
        this.weatherConfigFile = this.weatherConfigPath.resolve(CONFIG_NAME).toFile();

        boolean isClient = weatherEvents != null;
        boolean isPacket = biomeRegistry == null;

        if (isClient) {
            this.weatherEvents.putAll(weatherEvents);
        }
        if (!isPacket) {
            this.handleConfig(isClient);
        }

        this.weatherEvents.put("NONE", WeatherEvent.NONE);

        WeatherEvent currentWeatherEvent = this.weatherEvents.get(currentEvent);

        if (currentEvent != null && currentWeatherEvent == null) {
            BetterWeather.LOGGER.error("The last weather event: \"" + worldID.toString() + "\" was not found in: \"" + this.weatherEventsConfigPath.toString() + "\".\nDefaulting to weather event: \"NONE\".");
        } else {
            this.currentEvent = currentWeatherEvent;
            if (!isClient && !isPacket) {
                BetterWeather.LOGGER.info(worldID.toString() + " initialized with a weather event of: \"" + currentEvent + "\".");
            }
        }
    }


    public void handleConfig(boolean isClient) {
        File eventsDirectory = this.weatherEventsConfigPath.toFile();
        if (!eventsDirectory.exists()) {
            createDefaultEventConfigs();
        }


        File[] files = eventsDirectory.listFiles();

        if (files.length == 0) {
            createDefaultEventConfigs();
        }

        iterateAndReadConfiguredEvents(eventsDirectory.listFiles());
    }

    private void iterateAndReadConfiguredEvents(File[] files) {
        for (File configFile : files) {
            String absolutePath = configFile.getAbsolutePath();
            if (absolutePath.endsWith(".toml")) {
                CommentedConfig readConfig = configFile.exists() ? CommentedFileConfig.builder(configFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
                if (readConfig instanceof CommentedFileConfig) {
                    ((CommentedFileConfig) readConfig).load();
                }
                String name = configFile.getName().replace(".toml", "");
                this.weatherEvents.put(name, WeatherEvent.CODEC.decode(TomlCommentedConfigOps.INSTANCE, readConfig).resultOrPartial(BetterWeather.LOGGER::error).get().getFirst().setName(name));

            } else if (absolutePath.endsWith(".json")) {
                try {
                    String name = configFile.getName().replace(".json", "");
                    this.weatherEvents.put(name, WeatherEvent.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(new FileReader(configFile))).resultOrPartial(BetterWeather.LOGGER::error).get().getFirst().setName(name));
                } catch (FileNotFoundException e) {
                }
            }
        }
    }

    private void createEventConfig(WeatherEvent weatherEvent, ResourceLocation weatherEventID) {
        Path configFile = this.weatherEventsConfigPath.resolve(weatherEventID.getPath() + ".toml");
        CommentedConfig readConfig = configFile.toFile().exists() ? CommentedFileConfig.builder(configFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }
        CommentedConfig encodedConfig = (CommentedConfig) WeatherEvent.CODEC.encodeStart(TomlCommentedConfigOps.INSTANCE, weatherEvent).result().get();

        try {
            Files.createDirectories(configFile.getParent());
            new TomlWriter().write(configFile.toFile().exists() ? TomlCommentedConfigOps.recursivelyUpdateAndSortConfig(readConfig, encodedConfig) : encodedConfig, configFile, WritingMode.REPLACE);
        } catch (IOException e) {

        }
    }


    public void createDefaultEventConfigs() {
        for (WeatherEvent defaultEvent : WeatherEvent.DEFAULT_EVENTS) {
            Optional<RegistryKey<Codec<? extends WeatherEvent>>> optionalKey = BetterWeatherRegistry.WEATHER_EVENT.getOptionalKey(defaultEvent.codec());

            if (optionalKey.isPresent()) {
//                if (enabledWeatherEvents.contains(optionalKey.get())) {
                createEventConfig(defaultEvent, optionalKey.get().getLocation());
//                }
            } else {
                throw new IllegalStateException("Weather Event Key not there when requested: " + defaultEvent.getClass().getSimpleName());
            }
        }

    }

    @Override
    public boolean isLocalizedWeather() {
        return false;
    }

    @Override
    public String getCurrentWeatherEventKey() {
        return null;
    }

    @Override
    public WeatherEventSettings getCurrentWeatherEventSettings() {
        return new WeatherEventSettings() {
            @Override
            public double getTemperatureModifierAtPosition(BlockPos pos) {
                return 0;
            }

            @Override
            public double getHumidityModifierAtPosition(BlockPos pos) {
                return 0;
            }
        };
    }
}
