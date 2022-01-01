package corgitaco.betterweather.common.weather;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.common.network.NetworkHandler;
import corgitaco.betterweather.common.network.packet.weather.WeatherForecastChangedPacket;
import corgitaco.betterweather.common.savedata.WeatherEventSavedData;
import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.common.weather.event.Rain;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WeatherContext {

    public static final String CONFIG_NAME = "weather-settings.json";
    private static final WeatherEvent DEFAULT = Rain.DEFAULT;

    public static final Codec<WeatherContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(WeatherForecast.CODEC.fieldOf("weatherForecast").forGetter((weatherContext) -> {
            return weatherContext.weatherForecast;
        }), WeatherTimeSettings.CODEC.fieldOf("weatherTimeSettings").forGetter((weatherContext) -> {
            return weatherContext.weatherTimeSettings;
        }), ResourceLocation.CODEC.fieldOf("worldID").forGetter((weatherEventContext) -> {
            return weatherEventContext.worldID;
        }), Codec.unboundedMap(Codec.STRING, WeatherEvent.CODEC).fieldOf("weatherEvents").forGetter((weatherEventContext) -> {
            return weatherEventContext.weatherEvents;
        })).apply(builder, WeatherContext::new);
    });

    private final Map<String, WeatherEvent> weatherEvents = new HashMap<>();
    private final WeatherForecast weatherForecast;
    private final ResourceLocation worldID;
    private final Path weatherConfigPath;
    private final Path weatherEventsConfigPath;

    private final WeatherTimeSettings weatherTimeSettings;
    private final long dayLength; // TODO: Config
    private final long yearLengthInDays; // TODO: Config
    private final long minDaysBetweenEvents; // TODO: Config
    private final ArrayList<String> scrambledKeys = new ArrayList<>();
    private WeatherEvent currentEvent;
    private WeatherEvent lastEvent;
    private float strength;

    public WeatherContext(ServerWorld world) {
        this.worldID = world.dimension().location();
        this.weatherConfigPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("weather");
        this.weatherEventsConfigPath = this.weatherConfigPath.resolve("events");
        this.weatherTimeSettings = readOrCreateConfigJson(this.weatherConfigPath.resolve(CONFIG_NAME).toFile());
        this.dayLength = weatherTimeSettings.dayLength;
        this.yearLengthInDays = weatherTimeSettings.yearLength;
        this.minDaysBetweenEvents = weatherTimeSettings.minDaysBetweenWeatherEvents;
        handleEventConfigs(false);
        this.scrambledKeys.addAll(this.weatherEvents.keySet());
        this.weatherForecast = getAndComputeWeatherForecast(world).getForecast();
        assert weatherForecast != null;
        @Nullable
        WeatherEventInstance nextWeatherEvent = this.weatherForecast.getForecast().isEmpty() ? null : this.weatherForecast.getForecast().get(0);
        this.currentEvent = nextWeatherEvent == null ? DEFAULT : nextWeatherEvent.getDaysUntil((int) (world.getDayTime() / this.dayLength)) <= 0 && world.isNight() ? nextWeatherEvent.getEvent(this.weatherEvents) : DEFAULT;
    }

    // Packet Codec Constructor
    public WeatherContext(WeatherForecast weatherForecast, WeatherTimeSettings weatherTimeSettings, ResourceLocation worldID, Map<String, WeatherEvent> weatherEvents) {
        this(weatherForecast, weatherTimeSettings, worldID, weatherEvents, false);
    }

    // Client Constructor
    public WeatherContext(WeatherForecast weatherForecast, WeatherTimeSettings weatherTimeSettings, ResourceLocation worldID, Map<String, WeatherEvent> weatherEvents, boolean serializeClientOnlyConfigs) {
        this.worldID = worldID;
        this.weatherConfigPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("weather");
        this.weatherEventsConfigPath = this.weatherConfigPath.resolve("events");
        this.weatherEvents.putAll(weatherEvents);
        @Nullable
        WeatherEventInstance nextWeatherEvent = weatherForecast.getForecast().isEmpty() ? null : weatherForecast.getForecast().get(0);
        this.currentEvent = nextWeatherEvent == null ? DEFAULT : nextWeatherEvent.scheduledDay() == 0 ? nextWeatherEvent.getEvent(this.weatherEvents) : DEFAULT;
        this.weatherForecast = weatherForecast;
        this.weatherTimeSettings = weatherTimeSettings;
        this.dayLength = weatherTimeSettings.dayLength;
        this.yearLengthInDays = weatherTimeSettings.yearLength;
        this.minDaysBetweenEvents = weatherTimeSettings.minDaysBetweenWeatherEvents;
        if (serializeClientOnlyConfigs) {
            this.handleEventConfigs(true);
        }
        this.weatherEvents.forEach((key, event) -> event.setKey(key));
    }

    public WeatherEventSavedData getAndComputeWeatherForecast(ServerWorld world) {
        WeatherEventSavedData weatherEventSavedData = WeatherEventSavedData.get(world);
        if (weatherEventSavedData.getForecast() == null) {
            weatherEventSavedData.setForecast(computeWeatherForecast(world, new WeatherForecast(new ArrayList<>(), world.getDayTime())));
        }
        weatherEventSavedData.getForecast().getForecast().removeIf(weatherEventInstance -> !this.weatherEvents.containsKey(weatherEventInstance.getWeatherEventKey()));
        weatherEventSavedData.setForecast(weatherEventSavedData.getForecast());
        return weatherEventSavedData;
    }

    public WeatherForecast computeWeatherForecast(ServerWorld world, WeatherForecast weatherForecast) {
        return computeWeatherForecast(world, weatherForecast, 0L);
    }

    public WeatherForecast computeWeatherForecast(ServerWorld world, WeatherForecast weatherForecast, long seedModifier) {
        long dayTime = world.getDayTime();
        long lastCheckedTime = weatherForecast.getLastCheckedGameTime();
        @Nullable
        SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();


        long currentDay = dayTime / dayLength;
        long lastCheckedDay = lastCheckedTime / dayLength;

        if (lastCheckedDay < currentDay) {
            weatherForecast.getForecast().clear();
            weatherForecast.setLastCheckedGameTime(currentDay * dayLength);
            lastCheckedTime = weatherForecast.getLastCheckedGameTime();
            lastCheckedDay = lastCheckedTime / dayLength;
        }

        if (currentDay + this.yearLengthInDays == lastCheckedDay) {
            return weatherForecast;
        }

        List<WeatherEventInstance> newWeatherEvents = new ArrayList<>();

        Object2LongArrayMap<WeatherEvent> eventByLastTime = new Object2LongArrayMap<>();
        List<WeatherEventInstance> forecast = weatherForecast.getForecast();
        long lastDay = !forecast.isEmpty() ? forecast.get(forecast.size() - 1).scheduledDay() : currentDay;

        long day = lastCheckedDay;

        for (WeatherEventInstance weatherEventInstance : forecast) {
            eventByLastTime.put(weatherEventInstance.getEvent(this.weatherEvents), weatherEventInstance.scheduledDay());
        }

        for (; day <= currentDay + this.yearLengthInDays; day++) {
            dayTime += this.dayLength;
            Random random = new Random(world.getSeed() + world.dimension().location().hashCode() + day + seedModifier);
            Collections.shuffle(scrambledKeys, random);
            Season season = seasonContext == null ? null : seasonContext.getSeasonForTime(world.getDayTime());
            for (String key : scrambledKeys) {
                WeatherEvent value = this.weatherEvents.get(key);
                if ((/*day - eventByLastTime.getOrDefault(value, currentDay)) > value.getMinNumberOfNightsBetween() && (day - lastDay) > this.minDaysBetweenEvents &&*/ value.getChance(season) > random.nextDouble())) {
                    lastDay = day;
                    newWeatherEvents.add(new WeatherEventInstance(key, day));
                    eventByLastTime.put(value, day);
                }
            }
        }
        forecast.addAll(newWeatherEvents);
        weatherForecast.setLastCheckedGameTime(day * dayLength);
        return weatherForecast;
    }


    public void tick(World world) {
        WeatherEvent lastEvent = this.currentEvent;
        long currentDay = (world.getDayTime() / this.dayLength);
        if (!world.isClientSide) {
            List<ServerPlayerEntity> players = ((ServerWorld) world).players();
            updateForecast(world, currentDay, players);
            List<WeatherEventInstance> forecast = this.getWeatherForecast().getForecast();
            if (forecast.isEmpty()) {
                this.currentEvent = DEFAULT;
            } else {
                WeatherEventInstance nextEvent = forecast.get(0);
                this.currentEvent = nextEvent.getDaysUntil(currentDay) <= 0 && world.isNight() ? nextEvent.getEvent(this.weatherEvents) : DEFAULT;
            }

//            if (this.currentEvent != lastEvent) {
//                this.lastEvent = lastEvent;
//                this.strength = 0;
//                WeatherTextComponents.Notification endNotification = lastEvent.endNotification();
//                if (endNotification != null) {
//                    for (ServerPlayerEntity player : players) {
//                        player.displayClientMessage(endNotification.getCustomTranslationTextComponent(), endNotification.getNotificationType() == WeatherTextComponents.NotificationType.HOT_BAR);
//                    }
//                }
//
//                WeatherTextComponents.Notification startNotification = this.currentEvent.startNotification();
//                if (startNotification != null) {
//                    for (ServerPlayerEntity player : players) {
//                        player.displayClientMessage(startNotification.getCustomTranslationTextComponent(), startNotification.getNotificationType() == WeatherTextComponents.NotificationType.HOT_BAR);
//                    }
//                }
//                NetworkHandler.sendToAllPlayers(players, new WeatherEventChangedPacket(this.currentEvent.getKey()));
//            }
        }
        this.strength = MathHelper.clamp(this.strength + 0.01F, 0, 1.0F);
    }

    private void updateForecast(World world, long currentDay, List<ServerPlayerEntity> players) {
        updateForecast(world, currentDay);
        long lastCheckedGameTime = this.weatherForecast.getLastCheckedGameTime();
        WeatherForecast newWeatherForecast = computeWeatherForecast((ServerWorld) world, this.weatherForecast);

        long newLastCheckedGameTime = newWeatherForecast.getLastCheckedGameTime();
        long newLastCheckedDay = newLastCheckedGameTime / this.dayLength;
        long lastCheckedDay = lastCheckedGameTime / this.dayLength;
        if (newLastCheckedDay != lastCheckedDay) {
            NetworkHandler.sendToAllPlayers(players, new WeatherForecastChangedPacket(this.weatherForecast));
            WeatherEventSavedData.get(world).setForecast(weatherForecast);
        }
    }

    public void updateForecast(World world, long currentDay) {
        List<WeatherEventInstance> forecast = this.weatherForecast.getForecast();
        if (forecast.isEmpty()) {
            return;
        }

        WeatherEventInstance nextEvent = forecast.get(0);
        if (nextEvent.passed(currentDay)) {
            forecast.remove(0);
            NetworkHandler.sendToAllPlayers(((ServerWorld) world).players(), new WeatherForecastChangedPacket(this.weatherForecast));
            WeatherEventSavedData.get(world).setForecast(weatherForecast);
        }
    }

    public WeatherEvent getCurrentEvent() {
        return currentEvent;
    }

    @Nullable
    public WeatherEvent getLastEvent() {
        return lastEvent;
    }

    public void handleEventConfigs(boolean isClient) {
        if (isClient) {
            DEFAULT.setClient(DEFAULT.getClientSettings().createClientSettings(), "");
        }
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

        iterateAndReadConfiguredEvents(files, isClient);
    }

    private void iterateAndReadConfiguredEvents(File[] files, boolean isClient) {
        for (File configFile : files) {
            String absolutePath = configFile.getAbsolutePath();
//            if (absolutePath.endsWith(".toml")) {
//                readToml(isClient, configFile);

//            } else if (absolutePath.endsWith(".json")) {
            readJson(isClient, configFile);
//            }
        }
    }


    public void createDefaultEventConfigs() {
        for (Map.Entry<ResourceLocation, WeatherEvent> entry : BetterWeatherRegistry.DEFAULT_EVENTS.entrySet()) {
            ResourceLocation location = entry.getKey();
            WeatherEvent event = entry.getValue();
            Optional<RegistryKey<Codec<? extends WeatherEvent>>> optionalKey = BetterWeatherRegistry.WEATHER_EVENT.getResourceKey(event.codec());

            if (optionalKey.isPresent()) {
                createJsonEventConfig(event, location.toString());
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
            Optional<RegistryKey<Codec<? extends WeatherEvent>>> optionalKey = BetterWeatherRegistry.WEATHER_EVENT.getResourceKey(event.codec());

            if (optionalKey.isPresent()) {
                if (!tomlFile.exists() && !jsonFile.exists()) {
                    createJsonEventConfig(event, key);
                }
            } else {
                throw new IllegalStateException("Weather Event Key for codec not there when requested: " + event.getClass().getSimpleName());
            }
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


    private void readJson(boolean isClient, File configFile) {
        try {
            String noTypeFileName = configFile.getName().replace(".json", "");
            String name = noTypeFileName.toLowerCase();
            WeatherEvent decodedValue = WeatherEvent.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(new FileReader(configFile))).resultOrPartial(BetterWeather.LOGGER::error).get().getFirst().setKey(name);

            // We need to recreate the json each time to ensure we're taking into account any config fixing.
            createJsonEventConfig(decodedValue, noTypeFileName);

            if (isClient) {
                if (this.weatherEvents.containsKey(name)) {
                    WeatherEvent weatherEvent = this.weatherEvents.get(name);
                    weatherEvent.setClientSettings(decodedValue.getClientSettings());
                    weatherEvent.setClient(weatherEvent.getClientSettings().createClientSettings(), configFile.getAbsolutePath());
                }
            } else {
                this.weatherEvents.put(name, decodedValue);
            }
        } catch (FileNotFoundException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }

    private static WeatherTimeSettings readOrCreateConfigJson(File configFile) {
        if (!configFile.exists()) {
            try {
                Path path = configFile.toPath();
                Files.createDirectories(path.getParent());
                JsonElement jsonElement = WeatherTimeSettings.CODEC.encodeStart(JsonOps.INSTANCE, WeatherTimeSettings.DEFAULT).result().get();
                Files.write(path, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jsonElement).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return WeatherTimeSettings.CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(new FileReader(configFile))).result().orElseThrow(RuntimeException::new).getFirst();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public WeatherForecast getWeatherForecast() {
        return weatherForecast;
    }

    public Map<String, WeatherEvent> getWeatherEvents() {
        return weatherEvents;
    }

    public WeatherTimeSettings getWeatherTimeSettings() {
        return weatherTimeSettings;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public boolean isRefreshRenderers() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void setLastEvent(WeatherEvent lastEvent) {
        this.lastEvent = lastEvent;
    }

    @OnlyIn(Dist.CLIENT)
    public void setCurrentEvent(String currentEvent) {
        if (currentEvent.equals(DEFAULT.getKey())) {
            this.currentEvent = DEFAULT;
        } else {
            this.currentEvent = this.weatherEvents.get(currentEvent);
        }
    }

    public static class WeatherTimeSettings {
        public static final Codec<WeatherTimeSettings> CODEC = RecordCodecBuilder.create((builder) -> {
            return builder.group(Codec.LONG.fieldOf("daylength").forGetter((weatherTimeSettings) -> {
                return weatherTimeSettings.dayLength;
            }), Codec.LONG.fieldOf("yearLengthInDays").forGetter((weatherTimeSettings) -> {
                return weatherTimeSettings.yearLength;
            }), Codec.LONG.fieldOf("minDaysBetweenWeatherEvents").forGetter((weatherTimeSettings) -> {
                return weatherTimeSettings.minDaysBetweenWeatherEvents;
            })).apply(builder, WeatherTimeSettings::new);
        });

        public static final WeatherTimeSettings DEFAULT = new WeatherTimeSettings(24000, 100, 5);

        private final long dayLength;
        private final long yearLength;
        private final long minDaysBetweenWeatherEvents;

        public WeatherTimeSettings(long dayLength, long yearLength, long minDaysBetweenWeatherEvents) {
            this.dayLength = dayLength;
            this.yearLength = yearLength;
            this.minDaysBetweenWeatherEvents = minDaysBetweenWeatherEvents;
        }

        public long getDayLength() {
            return dayLength;
        }

        public long getYearLength() {
            return yearLength;
        }

        public long getMinDaysBetweenWeatherEvents() {
            return minDaysBetweenWeatherEvents;
        }
    }
}
