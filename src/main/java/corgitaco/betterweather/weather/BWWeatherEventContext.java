package corgitaco.betterweather.weather;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventContext;
import corgitaco.betterweather.api.weather.WeatherEventSettings;
import corgitaco.betterweather.data.storage.WeatherEventSavedData;
import corgitaco.betterweather.util.BetterWeatherUtil;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import corgitaco.betterweather.weather.event.Blizzard;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BWWeatherEventContext implements WeatherEventContext {
    public static final String CONFIG_NAME = "weather-settings.toml";

    public static final Codec<BWWeatherEventContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(ResourceLocation.CODEC.fieldOf("currentEvent").forGetter((weatherEventContext) -> {
            return BetterWeatherRegistry.WEATHER_EVENT.getKey(weatherEventContext.currentEvent.codec());
        }), ResourceLocation.CODEC.fieldOf("worldID").forGetter((weatherEventContext) -> {
            return weatherEventContext.worldID;
        }), Codec.list(ResourceLocation.CODEC).fieldOf("weather_events").forGetter((weatherEventContext) -> {
            Set<ResourceLocation> newMap = new HashSet<>();
            weatherEventContext.enabledWeatherEvents.forEach((weatherEventKey) -> {
                newMap.add(weatherEventKey.getLocation());
            });
            return new ArrayList<>(newMap);
        })).apply(builder, (currentEvent, worldID, weatherEvents) -> new BWWeatherEventContext(currentEvent, worldID, BetterWeatherUtil.transformWeatherLocationsToKeys(weatherEvents)));
    });


    private final Set<RegistryKey<Codec<? extends WeatherEvent>>> enabledWeatherEvents = new ReferenceArraySet<>();
    private final ResourceLocation worldID;
    private final Registry<Biome> biomeRegistry;
    private final Path weatherConfigPath;
    private final Path weatherEventsConfigPath;
    private final File weatherConfigFile;

    private WeatherEvent currentEvent;

    //Packet Constructor
    public BWWeatherEventContext(ResourceLocation name, ResourceLocation worldID, Set<RegistryKey<Codec<? extends WeatherEvent>>> weatherEvents) {
        this(RegistryKey.getOrCreateKey(BetterWeatherRegistry.WEATHER_EVENT_KEY, name), worldID, null, weatherEvents);
    }

    //Server world constructor
    public BWWeatherEventContext(WeatherEventSavedData weatherEventSavedData, RegistryKey<World> worldID, Registry<Biome> biomeRegistry) {
        this(RegistryKey.getOrCreateKey(BetterWeatherRegistry.WEATHER_EVENT_KEY, new ResourceLocation(weatherEventSavedData.getEvent() == null || weatherEventSavedData.getEvent().isEmpty() ? BetterWeatherRegistry.WEATHER_EVENT.getKey(Blizzard.CODEC).toString() : weatherEventSavedData.getEvent())), worldID.getLocation(), biomeRegistry, null);
    }

    public BWWeatherEventContext(RegistryKey<Codec<? extends WeatherEvent>> currentEvent, ResourceLocation worldID, @Nullable Registry<Biome> biomeRegistry, @Nullable Set<RegistryKey<Codec<? extends WeatherEvent>>> weatherEvents) {
        this.worldID = worldID;
        this.biomeRegistry = biomeRegistry;
        this.weatherConfigPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("weather");
        this.weatherEventsConfigPath = this.weatherConfigPath.resolve("events");
        this.weatherConfigFile = this.weatherConfigPath.resolve(CONFIG_NAME).toFile();

        boolean isClient = weatherEvents != null;
        boolean isPacket = biomeRegistry == null;

        if (isClient) {
            this.enabledWeatherEvents.addAll(weatherEvents);
        }
        if (!isPacket) {
            this.handleConfig(isClient);

            Path resolvedPath = this.weatherEventsConfigPath.resolve(currentEvent.getLocation().getNamespace());

            Path configFile = resolvedPath.resolve(currentEvent.getLocation().getPath() + ".toml");

            this.currentEvent = BetterWeatherRegistry.WEATHER_EVENT.getOrThrow(currentEvent).parse(TomlCommentedConfigOps.INSTANCE, configFile).get().left().get();
        }
    }


    public void handleConfig(boolean isClient) {
        processEventConfigs();
    }

    private void createEventConfig(WeatherEvent weatherEvent, ResourceLocation weatherEventID) {
        Path resolvedPath = this.weatherEventsConfigPath.resolve(weatherEventID.getNamespace());
        Path configFile = resolvedPath.resolve(weatherEventID.getPath() + ".toml");

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


    public void processEventConfigs() {
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
