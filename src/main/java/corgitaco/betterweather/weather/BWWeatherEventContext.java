package corgitaco.betterweather.weather;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlParser;
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
import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventContext;
import corgitaco.betterweather.api.weather.WeatherEventSettings;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.data.network.NetworkHandler;
import corgitaco.betterweather.data.network.packet.util.RefreshRenderersPacket;
import corgitaco.betterweather.data.network.packet.weather.WeatherDataPacket;
import corgitaco.betterweather.data.network.packet.weather.WeatherForecastPacket;
import corgitaco.betterweather.data.storage.WeatherEventSavedData;
import corgitaco.betterweather.helpers.BiomeUpdate;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import corgitaco.betterweather.weather.event.None;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("ConstantConditions")
public class BWWeatherEventContext implements WeatherEventContext {

    public static final String CONFIG_NAME = "weather-settings.toml";
    private static final String DEFAULT = "betterweather-none";

    public static final Codec<BWWeatherEventContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.STRING.fieldOf("currentEvent").forGetter((weatherEventContext) -> {
            return weatherEventContext.currentEvent.getName();
        }), Codec.BOOL.fieldOf("weatherForced").forGetter((weatherEventContext) -> {
            return weatherEventContext.weatherForced;
        }), Codec.list(WeatherInstance.PACKET_CODEC).fieldOf("forecast").forGetter((weatherEventContext) -> {
            return weatherEventContext.forecast;
        }), ResourceLocation.CODEC.fieldOf("worldID").forGetter((weatherEventContext) -> {
            return weatherEventContext.worldID;
        }), Codec.unboundedMap(Codec.STRING, WeatherEvent.CODEC).fieldOf("weatherEvents").forGetter((weatherEventContext) -> {
            return weatherEventContext.weatherEvents;
        })).apply(builder, BWWeatherEventContext::new);
    });

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(), (map) -> {
        map.put("changeBiomeColors", "Do weather events change biome vegetation colors? This will cause chunks to refresh (F3+A).");
    }), true);


    private final Map<String, WeatherEvent> weatherEvents = new HashMap<>();
    private final List<WeatherInstance> forecast;
    private final ResourceLocation worldID;
    private final Registry<Biome> biomeRegistry;
    private final Path weatherConfigPath;
    private final Path weatherEventsConfigPath;
    private final File weatherConfigFile;

    private boolean refreshRenderers;
    private WeatherEvent currentEvent;
    private boolean weatherForced;

    //Packet Constructor
    public BWWeatherEventContext(String currentEvent, boolean weatherForced, List<WeatherInstance> forecast, ResourceLocation worldID, Map<String, WeatherEvent> weatherEvents) {
        this(currentEvent, weatherForced, forecast, worldID, null, weatherEvents);
    }

    //Server world constructor
    public BWWeatherEventContext(WeatherEventSavedData weatherEventSavedData, RegistryKey<World> worldID, Registry<Biome> biomeRegistry) {
        this(weatherEventSavedData.getEvent(), weatherEventSavedData.isWeatherForced(), weatherEventSavedData.getForecast(), worldID.getLocation(), biomeRegistry, null);
    }

    public BWWeatherEventContext(String currentEvent, boolean weatherForced, List<WeatherInstance> forecast, ResourceLocation worldID, @Nullable Registry<Biome> biomeRegistry, @Nullable Map<String, WeatherEvent> weatherEvents) {
        this.worldID = worldID;
        this.biomeRegistry = biomeRegistry;
        this.weatherConfigPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("weather");
        this.weatherEventsConfigPath = this.weatherConfigPath.resolve("events");
        this.weatherConfigFile = this.weatherConfigPath.resolve(CONFIG_NAME).toFile();
        this.weatherEvents.put(DEFAULT, None.DEFAULT.setName(DEFAULT));
        this.weatherForced = weatherForced;
        boolean isClient = weatherEvents != null;
        boolean isPacket = biomeRegistry == null;
        this.forecast = forecast;

        if (isClient) {
            this.weatherEvents.putAll(weatherEvents);

            this.weatherEvents.forEach((key, weatherEvent) -> {
                weatherEvent.setClient(weatherEvent.getClientSettings().createClientSettings());
            });
        }
        if (!isPacket) {
            this.handleConfig(isClient);
        }

        WeatherEvent currentWeatherEvent = this.weatherEvents.get(currentEvent);
        this.currentEvent = this.weatherEvents.getOrDefault(currentEvent, None.DEFAULT);
        if (currentEvent != null && currentWeatherEvent == null) {
            BetterWeather.LOGGER.error("The last weather event for the world: \"" + worldID.toString() + "\" was not found in: \"" + this.weatherEventsConfigPath.toString() + "\".\nDefaulting to weather event: \"" + DEFAULT + "\".");
        } else {
            if (!isClient && !isPacket) {
                BetterWeather.LOGGER.info(worldID.toString() + " initialized with a weather event of: \"" + (currentEvent == null ? DEFAULT : currentEvent) + "\".");
            }
        }
        if (!isPacket) {
            for (Map.Entry<String, WeatherEvent> stringWeatherEventEntry : this.weatherEvents.entrySet()) {
                stringWeatherEventEntry.getValue().fillBiomes(biomeRegistry);
            }
        }
    }


    public void updateForecast(World world) {
        long gameTime = world.getGameTime();
        if (world instanceof ServerWorld) {
            if (this.forecast.isEmpty()) {
                Random random = new Random(((ServerWorld) world).getSeed() + gameTime);
                WeatherEvent randomEvent = getRandomEvent(world, random);

                while (randomEvent == null) {
                    randomEvent = getRandomEvent(world, random);
                }
                WeatherInstance weatherInstance = new WeatherInstance(randomEvent.getName(), (random.nextInt(168000) + 12000), random.nextInt(12000) + 12000);

                this.forecast.add(weatherInstance);
            }
        }
        WeatherInstance firstWeatherEvent = this.forecast.get(0);

        if (firstWeatherEvent.getTimeUntilEvent() <= 0) {
            if (firstWeatherEvent.getEventTime() <= 0) {
                this.forecast.remove(0);
            } else {
                firstWeatherEvent.setEventTime(firstWeatherEvent.getEventTime() - 1);
            }
        } else {
            firstWeatherEvent.setTimeUntilEvent(firstWeatherEvent.getTimeUntilEvent() - 1L);
        }

        for (int i = 1; i < 100; i++) {
            WeatherInstance lastWeatherInstance = this.forecast.get(i - 1);
            long timeUntil = lastWeatherInstance.getTimeUntilEvent() + lastWeatherInstance.getTimeUntilEvent();
            if (world instanceof ServerWorld) {
                if (this.forecast.size() <= i) {
                    Random random = new Random(((ServerWorld) world).getSeed() + gameTime + timeUntil);

                    WeatherEvent randomEvent = getRandomEvent(world, random);

                    while (randomEvent == null) {
                        randomEvent = getRandomEvent(world, random);
                    }

                    WeatherInstance weatherInstance = new WeatherInstance(randomEvent.getName(), timeUntil + (random.nextInt(168000) + 12000), random.nextInt(12000) + 12000);
                    this.forecast.add(weatherInstance);
                    NetworkHandler.sendToAllPlayers(((ServerWorld) world).getPlayers(), new WeatherForecastPacket(this.forecast));
                }
            }

            WeatherInstance weatherInstance = this.forecast.get(i);
            weatherInstance.setTimeUntilEvent(timeUntil);
        }
        // Sync client.
        if (world instanceof ServerWorld) {
            if (gameTime % 600L == 0) {
                NetworkHandler.sendToAllPlayers(((ServerWorld) world).getPlayers(), new WeatherForecastPacket(this.forecast));
            }
        }
    }

    @Nullable
    public WeatherEvent getRandomEvent(World world, Random random) {
        ArrayList<String> list = new ArrayList<>(this.weatherEvents.keySet());
        Collections.shuffle(list, random);
        Season season = ((Climate) world).getSeason();
        boolean hasSeasons = season != null;

        for (String entry : list) {
            if (entry.equals(DEFAULT)) {
                continue;
            }
            WeatherEvent weatherEvent = this.weatherEvents.get(entry);
            double chance = hasSeasons ? weatherEvent.getSeasonChances().getOrDefault(season.getKey(), new IdentityHashMap<>()).getOrDefault(season.getPhase(), weatherEvent.getDefaultChance()) : weatherEvent.getDefaultChance();

            if (random.nextDouble() < chance) {
                return weatherEvent;
            }
        }

        return null;
    }


    public void tick(World world) {
        //TODO: Remove this check and figure out what could possibly be causing this and prevent it.
        if (this.weatherEvents.get(DEFAULT) == this.currentEvent && world.isRaining()) {
            world.getWorldInfo().setRaining(false);
        }

        WeatherEvent prevEvent = this.currentEvent;
        boolean wasForced = this.weatherForced;
        if (world instanceof ServerWorld) {
            shuffleAndPickWeatherEvent(world);
            this.updateForecast((ServerWorld) world);
        }

        if (prevEvent != this.currentEvent || wasForced != this.weatherForced) {
            onWeatherChange(world);
        }
        if (world instanceof ServerWorld) {
            this.currentEvent.worldTick((ServerWorld) world, world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED), world.getGameTime());
        }
        if (world.isRemote) {
            this.getCurrentClientEvent().clientTick((ClientWorld) world, world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED), world.getGameTime(), Minecraft.getInstance(), currentEvent::isValidBiome);
        }
    }

    private void onWeatherChange(World world) {
        ((BiomeUpdate) world).updateBiomeData();
        save(world);
        if (world instanceof ServerWorld) {
            ((IServerWorldInfo) world.getWorldInfo()).setThundering(this.currentEvent.isThundering());
            sendPackets((ServerWorld) world);
        }
    }

    private void sendPackets(ServerWorld world) {
        NetworkHandler.sendToAllPlayers(world.getPlayers(), new WeatherDataPacket(this));
        if (this.refreshRenderers) {
            NetworkHandler.sendToAllPlayers(world.getPlayers(), new RefreshRenderersPacket());
        }
    }

    private void shuffleAndPickWeatherEvent(World world) {
        boolean isPrecipitation = world.getWorldInfo().isRaining();
        Season season = ((Climate) world).getSeason();
        boolean hasSeasons = season != null;
        float rainingStrength = world.rainingStrength;
        if (isPrecipitation) {
            if (rainingStrength <= 0.02F) {
                if (!this.weatherForced) {
                    Random random = new Random(((ServerWorld) world).getSeed() + world.getGameTime());
                    ArrayList<String> list = new ArrayList<>(this.weatherEvents.keySet());
                    Collections.shuffle(list, random);
                    for (String entry : list) {
                        if (entry.equals(DEFAULT)) {
                            continue;
                        }
                        WeatherEvent weatherEvent = this.weatherEvents.get(entry);
                        double chance = hasSeasons ? weatherEvent.getSeasonChances().getOrDefault(season.getKey(), new IdentityHashMap<>()).getOrDefault(season.getPhase(), weatherEvent.getDefaultChance()) : weatherEvent.getDefaultChance();

                        if (random.nextDouble() < chance || this.currentEvent == this.weatherEvents.get(DEFAULT)) {
                            this.currentEvent = weatherEvent;
                            break;
                        }
                    }
                }
            }
        } else {
            if (rainingStrength == 0.0F) {
                this.currentEvent = this.weatherEvents.get(DEFAULT);
                this.weatherForced = false;
            }
        }
    }

    private void save(World world) {
        WeatherEventSavedData weatherEventSavedData = WeatherEventSavedData.get(world);
        weatherEventSavedData.setEvent(this.currentEvent.getName());
        weatherEventSavedData.setWeatherForced(this.weatherForced);
    }

    public WeatherEvent weatherForcer(String weatherEventName, int weatherEventLength, ServerWorld world) {
        this.currentEvent = this.weatherEvents.get(weatherEventName);
        this.weatherForced = true;

        IServerWorldInfo worldInfo = (IServerWorldInfo) world.getWorldInfo();
        boolean isDefault = weatherEventName.equals(DEFAULT);

        if (isDefault) {
            worldInfo.setClearWeatherTime(weatherEventLength);
        } else {
            worldInfo.setClearWeatherTime(0);
            worldInfo.setRainTime(weatherEventLength);
            worldInfo.setRaining(true);
            worldInfo.setThundering(this.currentEvent.isThundering());
        }

        onWeatherChange(world);
        return this.currentEvent;
    }


    public void handleConfig(boolean isClient) {
        if (!this.weatherConfigFile.exists()) {
            createDefaultWeatherConfigFile();
        } else {
            try (Reader reader = new FileReader(this.weatherConfigFile)) {
                Optional<WeatherEventConfig> configHolder = WeatherEventConfig.CODEC.parse(CONFIG_OPS, new TomlParser().parse(reader)).resultOrPartial(BetterWeather.LOGGER::error);

                if (configHolder.isPresent()) {
                    this.refreshRenderers = configHolder.get().changeBiomeColors;
                } else {
                    BetterWeather.LOGGER.error("\"" + this.weatherConfigFile.toString() + "\" not there when requested.");
                }
            } catch (IOException e) {
                BetterWeather.LOGGER.error(e.toString());
            }
        }

        handleEventConfigs(isClient);
    }

    private void createDefaultWeatherConfigFile() {
        CommentedConfig readConfig = this.weatherConfigFile.exists() ? CommentedFileConfig.builder(this.weatherConfigFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }

        CommentedConfig encodedConfig = (CommentedConfig) WeatherEventConfig.CODEC.encodeStart(CONFIG_OPS, WeatherEventConfig.DEFAULT).result().get();
        try {
            Files.createDirectories(this.weatherConfigFile.toPath().getParent());
            new TomlWriter().write(this.weatherConfigFile.exists() ? TomlCommentedConfigOps.recursivelyUpdateAndSortConfig(readConfig, encodedConfig) : encodedConfig, this.weatherConfigFile, WritingMode.REPLACE);
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }

    private void handleEventConfigs(boolean isClient) {
        File eventsDirectory = this.weatherEventsConfigPath.toFile();
        if (!eventsDirectory.exists()) {
            createDefaultEventConfigs();
        }

        File[] files = eventsDirectory.listFiles();

        if (files.length == 0) {
            createDefaultEventConfigs();
        }

        if (isClient) {
            addSettingsIfMissing();
        }

        iterateAndReadConfiguredEvents(eventsDirectory.listFiles(), isClient);
    }

    private void iterateAndReadConfiguredEvents(File[] files, boolean isClient) {
        for (File configFile : files) {
            String absolutePath = configFile.getAbsolutePath();
            if (absolutePath.endsWith(".toml")) {
                readToml(isClient, configFile);

            } else if (absolutePath.endsWith(".json")) {
                readJson(isClient, configFile);
            }
        }
    }

    private void readJson(boolean isClient, File configFile) {
        try {
            String name = configFile.getName().replace(".json", "").toLowerCase();
            WeatherEvent decodedValue = WeatherEvent.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(new FileReader(configFile))).resultOrPartial(BetterWeather.LOGGER::error).get().getFirst().setName(name);
            if (isClient && !BetterWeather.CLIENT_CONFIG.useServerClientSettings) {
                if (this.weatherEvents.containsKey(name)) {
                    WeatherEvent weatherEvent = this.weatherEvents.get(name);
                    weatherEvent.setClientSettings(decodedValue.getClientSettings());
                    weatherEvent.setClient(weatherEvent.getClientSettings().createClientSettings());
                }
            } else {
                this.weatherEvents.put(name, decodedValue);
            }
        } catch (FileNotFoundException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }

    private void readToml(boolean isClient, File configFile) {
        CommentedConfig readConfig = configFile.exists() ? CommentedFileConfig.builder(configFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }
        String name = configFile.getName().replace(".toml", "").toLowerCase();
        WeatherEvent decodedValue = WeatherEvent.CODEC.decode(TomlCommentedConfigOps.INSTANCE, readConfig).resultOrPartial(BetterWeather.LOGGER::error).get().getFirst().setName(name);

        if (isClient && !BetterWeather.CLIENT_CONFIG.useServerClientSettings) {
            if (this.weatherEvents.containsKey(name)) {
                WeatherEvent weatherEvent = this.weatherEvents.get(name);
                weatherEvent.setClientSettings(decodedValue.getClientSettings());
                weatherEvent.setClient(weatherEvent.getClientSettings().createClientSettings());
            }
        } else {
            this.weatherEvents.put(name, decodedValue);
        }
    }

    private void createTomlEventConfig(WeatherEvent weatherEvent, String weatherEventID) {
        Path configFile = this.weatherEventsConfigPath.resolve(weatherEventID.replace(":", "-") + ".toml");
        CommentedConfig readConfig = configFile.toFile().exists() ? CommentedFileConfig.builder(configFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }
        CommentedConfig encodedConfig = (CommentedConfig) WeatherEvent.CODEC.encodeStart(weatherEvent.configOps(), weatherEvent).result().get();

        try {
            Files.createDirectories(configFile.getParent());
            new TomlWriter().write(configFile.toFile().exists() ? TomlCommentedConfigOps.recursivelyUpdateAndSortConfig(readConfig, encodedConfig) : encodedConfig, configFile, WritingMode.REPLACE);
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }

    private void createJsonEventConfig(WeatherEvent weatherEvent, String weatherEventID) {
        Path configFile = this.weatherEventsConfigPath.resolve(weatherEventID.replace(":", "-") + ".json");
        JsonElement jsonElement = WeatherEvent.CODEC.encodeStart(JsonOps.INSTANCE, weatherEvent).result().get();

        try {
            Files.createDirectories(configFile.getParent());
            Files.write(configFile, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jsonElement).getBytes());
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }


    public void createDefaultEventConfigs() {
        for (Map.Entry<ResourceLocation, WeatherEvent> entry : BetterWeatherRegistry.DEFAULT_EVENTS.entrySet()) {
            ResourceLocation location = entry.getKey();
            WeatherEvent event = entry.getValue();
            Optional<RegistryKey<Codec<? extends WeatherEvent>>> optionalKey = BetterWeatherRegistry.WEATHER_EVENT.getOptionalKey(event.codec());

            if (optionalKey.isPresent()) {
                if (BetterWeatherConfig.SERIALIZE_AS_JSON) {
                    createJsonEventConfig(event, location.toString());
                } else {
                    createTomlEventConfig(event, location.toString());
                }
            } else {
                throw new IllegalStateException("Weather Event Key for codec not there when requested: " + event.getClass().getSimpleName());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addSettingsIfMissing() {
        for (Map.Entry<String, WeatherEvent> entry : this.weatherEvents.entrySet()) {
            WeatherEvent event = entry.getValue();
            String key = entry.getKey();
            File tomlFile = this.weatherEventsConfigPath.resolve(key + ".toml").toFile();
            File jsonFile = this.weatherEventsConfigPath.resolve(key + ".json").toFile();
            Optional<RegistryKey<Codec<? extends WeatherEvent>>> optionalKey = BetterWeatherRegistry.WEATHER_EVENT.getOptionalKey(event.codec());

            if (optionalKey.isPresent()) {
                if (!tomlFile.exists() && !jsonFile.exists()) {
                    if (BetterWeatherConfig.SERIALIZE_AS_JSON) {
                        createJsonEventConfig(event, key);
                    } else {
                        createTomlEventConfig(event, key);
                    }
                }
            } else {
                throw new IllegalStateException("Weather Event Key for codec not there when requested: " + event.getClass().getSimpleName());
            }
        }
    }

    public void setCurrentEvent(WeatherEvent currentEvent) {
        this.currentEvent = currentEvent;
    }

    public void setCurrentEvent(String currentEvent) {
        this.currentEvent = this.weatherEvents.get(currentEvent);
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

    public List<WeatherInstance> getForecast() {
        return forecast;
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

    public boolean isRefreshRenderers() {
        return refreshRenderers;
    }

    @OnlyIn(Dist.CLIENT)
    public WeatherEventClient<?> getCurrentClientEvent() {
        return this.currentEvent.getClient();
    }

    private static class WeatherEventConfig {
        public static final WeatherEventConfig DEFAULT = new WeatherEventConfig(true);

        public static Codec<WeatherEventConfig> CODEC = RecordCodecBuilder.create((builder) -> {
            return builder.group(Codec.BOOL.fieldOf("changeBiomeColors").forGetter((weatherEventConfig) -> {
                return weatherEventConfig.changeBiomeColors;
            })).apply(builder, WeatherEventConfig::new);
        });

        private final boolean changeBiomeColors;

        private WeatherEventConfig(boolean changeBiomeColors) {
            this.changeBiomeColors = changeBiomeColors;
        }
    }
}
