package corgitaco.betterweather.weather;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.Climate;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventContext;
import corgitaco.betterweather.api.weather.WeatherEventSettings;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.data.network.NetworkHandler;
import corgitaco.betterweather.data.network.packet.weather.WeatherPacket;
import corgitaco.betterweather.data.storage.WeatherEventSavedData;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BWWeatherEventContext implements WeatherEventContext {
    public static final String CONFIG_NAME = "weather-settings.toml";

    public static final Codec<BWWeatherEventContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.STRING.fieldOf("currentEvent").forGetter((weatherEventContext) -> {
            return weatherEventContext.currentEvent.getName();
        }), Codec.BOOL.fieldOf("weatherForced").forGetter((weatherEventContext) -> {
            return weatherEventContext.weatherForced;
        }), ResourceLocation.CODEC.fieldOf("worldID").forGetter((weatherEventContext) -> {
            return weatherEventContext.worldID;
        }), Codec.unboundedMap(Codec.STRING, WeatherEvent.CODEC).fieldOf("weatherEvents").forGetter((weatherEventContext) -> {
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

    private boolean weatherForced;

    //Packet Constructor
    public BWWeatherEventContext(String currentEvent, boolean weatherForced, ResourceLocation worldID, Map<String, WeatherEvent> weatherEvents) {
        this(currentEvent, weatherForced, worldID, null, weatherEvents);
    }

    //Server world constructor
    public BWWeatherEventContext(WeatherEventSavedData weatherEventSavedData, RegistryKey<World> worldID, Registry<Biome> biomeRegistry) {
        this(weatherEventSavedData.getEvent(), weatherEventSavedData.isWeatherForced(), worldID.getLocation(), biomeRegistry, null);
    }

    public BWWeatherEventContext(String currentEvent, boolean weatherForced, ResourceLocation worldID, @Nullable Registry<Biome> biomeRegistry, @Nullable Map<String, WeatherEvent> weatherEvents) {
        this.worldID = worldID;
        this.biomeRegistry = biomeRegistry;
        this.weatherConfigPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("weather");
        this.weatherEventsConfigPath = this.weatherConfigPath.resolve("events");
        this.weatherConfigFile = this.weatherConfigPath.resolve(CONFIG_NAME).toFile();
        this.weatherEvents.put("none", WeatherEvent.NONE.setName("none"));
        this.weatherForced = weatherForced;
        boolean isClient = weatherEvents != null;
        boolean isPacket = biomeRegistry == null;

        if (isClient) {
            this.weatherEvents.putAll(weatherEvents);
        }
        if (!isPacket) {
            this.handleConfig();
        }


        WeatherEvent currentWeatherEvent = this.weatherEvents.get(currentEvent);

        if (currentEvent != null && currentWeatherEvent == null) {
            BetterWeather.LOGGER.error("The last weather event: \"" + worldID.toString() + "\" was not found in: \"" + this.weatherEventsConfigPath.toString() + "\".\nDefaulting to weather event: \"NONE\".");
        } else {
            this.currentEvent = this.weatherEvents.getOrDefault(currentEvent, WeatherEvent.NONE);
            if (!isClient && !isPacket) {
                BetterWeather.LOGGER.info(worldID.toString() + " initialized with a weather event of: \"" + (currentEvent == null ? "none" : currentEvent) + "\".");
            }
        }
    }


    public void tick(World world) {
        WeatherEvent prevEvent = this.currentEvent;
        boolean wasForced = this.weatherForced;
        if (world instanceof ServerWorld) {
            shuffleAndPickWeatherEvent(world);
        }

        if (prevEvent != this.currentEvent || wasForced != this.weatherForced) {
            save(world);
            NetworkHandler.sendToAllPlayers(((ServerWorld) world).getPlayers(), new WeatherPacket(this));
        }
    }

    private void shuffleAndPickWeatherEvent(World world) {
        boolean isPrecipitation = world.getWorldInfo().isRaining() || world.getWorldInfo().isThundering();
        Season season = ((Climate) world).getSeason();
        boolean hasSeasons = season != null;
        if (world.rainingStrength == 0.0F) {
            if (isPrecipitation) {
//                if (!this.weatherForced) {
                    Random random = new Random(((ServerWorld) world).getSeed() + world.getGameTime());
                    ArrayList<String> list = new ArrayList<>(this.weatherEvents.keySet());
                    Collections.shuffle(list, random);
                    for (String entry : list) {
                        if (entry.equals("none")) {
                            continue;
                        }
                        WeatherEvent weatherEvent = this.weatherEvents.get(entry);
                        double chance = hasSeasons ? weatherEvent.getSeasonChances().getOrDefault(season.getKey(), new IdentityHashMap<>()).getOrDefault(season.getPhase(), weatherEvent.getDefaultChance()) : weatherEvent.getDefaultChance();

                        if (random.nextDouble() < chance) {
                            this.currentEvent = weatherEvent;
                            break;
                        }
                    }
//                }
            } else {
                this.currentEvent = this.weatherEvents.get("none");
                this.weatherForced = false;
            }
        }
    }

    private void save(World world) {
        WeatherEventSavedData weatherEventSavedData = WeatherEventSavedData.get(world);
        weatherEventSavedData.setEvent(this.currentEvent.getName());
        weatherEventSavedData.setWeatherForced(this.weatherForced);
    }


    public void handleConfig() {
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
                String name = configFile.getName().replace(".toml", "").toLowerCase();
                this.weatherEvents.put(name, WeatherEvent.CODEC.decode(TomlCommentedConfigOps.INSTANCE, readConfig).resultOrPartial(BetterWeather.LOGGER::error).get().getFirst().setName(name));

            } else if (absolutePath.endsWith(".json")) {
                try {
                    String name = configFile.getName().replace(".json", "").toLowerCase();
                    this.weatherEvents.put(name, WeatherEvent.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(new FileReader(configFile))).resultOrPartial(BetterWeather.LOGGER::error).get().getFirst().setName(name));
                } catch (FileNotFoundException e) {
                }
            }
        }
    }

    private void createTomlEventConfig(WeatherEvent weatherEvent, ResourceLocation weatherEventID) {
        Path configFile = this.weatherEventsConfigPath.resolve(weatherEventID.getPath() + ".toml");
        CommentedConfig readConfig = configFile.toFile().exists() ? CommentedFileConfig.builder(configFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }
        CommentedConfig encodedConfig = (CommentedConfig) WeatherEvent.CODEC.encodeStart(weatherEvent.configOps(), weatherEvent).result().get();

        try {
            Files.createDirectories(configFile.getParent());
            new TomlWriter().write(configFile.toFile().exists() ? TomlCommentedConfigOps.recursivelyUpdateAndSortConfig(readConfig, encodedConfig) : encodedConfig, configFile, WritingMode.REPLACE);
        } catch (IOException e) {

        }
    }

    private void createJsonEventConfig(WeatherEvent weatherEvent, ResourceLocation weatherEventID) {
        Path configFile = this.weatherEventsConfigPath.resolve(weatherEventID.getPath() + ".json");
        CommentedConfig readConfig = configFile.toFile().exists() ? CommentedFileConfig.builder(configFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }
        JsonElement jsonElement = WeatherEvent.CODEC.encodeStart(JsonOps.INSTANCE, weatherEvent).result().get();

        try {
            Files.createDirectories(configFile.getParent());
            Files.write(configFile, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jsonElement).getBytes());
        } catch (IOException e) {

        }
    }


    public void createDefaultEventConfigs() {
        for (WeatherEvent defaultEvent : WeatherEvent.DEFAULT_EVENTS) {
            Optional<RegistryKey<Codec<? extends WeatherEvent>>> optionalKey = BetterWeatherRegistry.WEATHER_EVENT.getOptionalKey(defaultEvent.codec());

            if (optionalKey.isPresent()) {
                ResourceLocation location = optionalKey.get().getLocation();
                if (BetterWeatherConfig.SERIALIZE_AS_JSON) {
                    createJsonEventConfig(defaultEvent, location);
                } else {
                    createTomlEventConfig(defaultEvent, location);
                }
            } else {
                throw new IllegalStateException("Weather Event Key not there when requested: " + defaultEvent.getClass().getSimpleName());
            }
        }

    }

    public void setCurrentEvent(WeatherEvent currentEvent) {
        this.currentEvent = currentEvent;
    }

    public void setWeatherForced(boolean weatherForced) {
        this.weatherForced = weatherForced;
    }

    public WeatherEvent getCurrentEvent() {
        return currentEvent;
    }

    public Map<String, WeatherEvent> getWeatherEvents() {
        return weatherEvents;
    }

    public boolean isWeatherForced() {
        return weatherForced;
    }

    @Override
    public boolean isLocalizedWeather() {
        return false;
    }

    @Override
    public String getCurrentWeatherEventKey() {
        return this.currentEvent.getName();
    }

    @Override
    public WeatherEventSettings getCurrentWeatherEventSettings() {
        return this.currentEvent;
    }
}
