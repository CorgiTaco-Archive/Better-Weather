package corgitaco.betterweather.common.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.common.network.NetworkHandler;
import corgitaco.betterweather.common.network.packet.weather.WeatherEventChangedPacket;
import corgitaco.betterweather.common.network.packet.weather.WeatherForecastChangedPacket;
import corgitaco.betterweather.common.savedata.WeatherEventSavedData;
import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.common.weather.event.None;
import corgitaco.betterweather.common.weather.event.client.NoneClient;
import corgitaco.betterweather.common.weather.event.client.settings.NoneClientSettings;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;

import static corgitaco.betterweather.common.weather.config.WeatherConfigSerializers.handleEventConfigs;
import static corgitaco.betterweather.common.weather.config.WeatherConfigSerializers.readOrCreateConfigJson;

public class WeatherContext {

    public static final String CONFIG_NAME = "weather-settings.json";
    private static final WeatherEvent DEFAULT = None.DEFAULT.setKey("none");

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
        handleEventConfigs(false, weatherEvents, weatherEventsConfigPath);
        this.scrambledKeys.addAll(this.weatherEvents.keySet());
        this.weatherForecast = getAndComputeWeatherForecast(world).getForecast();
        assert weatherForecast != null;
        @Nullable
        WeatherEventInstance nextWeatherEvent = this.weatherForecast.getForecast().isEmpty() ? null : this.weatherForecast.getForecast().get(0);
        this.currentEvent = nextWeatherEvent == null ? DEFAULT : nextWeatherEvent.active(world.getDayTime(), this.dayLength) ? nextWeatherEvent.getEvent(this.weatherEvents) : DEFAULT;
        for (Map.Entry<String, WeatherEvent> entry : this.weatherEvents.entrySet()) {
            entry.getValue().setKey(entry.getKey());
            entry.getValue().fillBiomes(world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
        }
    }

    // Packet Codec Constructor
    public WeatherContext(WeatherForecast weatherForecast, WeatherTimeSettings weatherTimeSettings, ResourceLocation worldID, Map<String, WeatherEvent> weatherEvents) {
        this(weatherForecast, weatherTimeSettings, worldID, weatherEvents, false);
    }

    // Client Constructor
    public WeatherContext(WeatherForecast weatherForecast, WeatherTimeSettings weatherTimeSettings, ResourceLocation worldID, Map<String, WeatherEvent> weatherEvents, boolean serializeClientOnlyConfigs) {
        ClientWorld level = Minecraft.getInstance().level;
        this.worldID = worldID;
        this.weatherConfigPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("weather");
        this.weatherEventsConfigPath = this.weatherConfigPath.resolve("events");
        this.weatherEvents.putAll(weatherEvents);
        this.dayLength = weatherTimeSettings.dayLength;
        @Nullable
        WeatherEventInstance nextWeatherEvent = weatherForecast.getForecast().isEmpty() ? null : weatherForecast.getForecast().get(0);
        this.currentEvent = nextWeatherEvent == null ? DEFAULT : nextWeatherEvent.active(level.getDayTime(), this.getDayLength()) ? nextWeatherEvent.getEvent(this.weatherEvents) : DEFAULT;
        this.weatherForecast = weatherForecast;
        this.weatherTimeSettings = weatherTimeSettings;
        this.yearLengthInDays = weatherTimeSettings.yearLength;
        this.minDaysBetweenEvents = weatherTimeSettings.minDaysBetweenWeatherEvents;
        if (serializeClientOnlyConfigs) {
            handleEventConfigs(true, weatherEvents, weatherEventsConfigPath);
        }
        if (DEFAULT.getClient() == null) {
            DEFAULT.setClient(new NoneClient(new NoneClientSettings(new ColorSettings())), "internal");
        }

        for (Map.Entry<String, WeatherEvent> entry : this.weatherEvents.entrySet()) {
            entry.getValue().setKey(entry.getKey());
            entry.getValue().fillBiomes(level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
        }
    }

    public WeatherEventSavedData getAndComputeWeatherForecast(ServerWorld world) {
        WeatherEventSavedData weatherEventSavedData = WeatherEventSavedData.get(world);
        if (weatherEventSavedData.getForecast() == null) {
            weatherEventSavedData.setForecast(computeWeatherForecast(world, new WeatherForecast(new ArrayList<>(), new ArrayList<>(), world.getDayTime())));
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

        if (currentDay + this.yearLengthInDays <= lastCheckedDay) {
            return weatherForecast;
        }

        List<WeatherEventInstance> newWeatherEvents = new ArrayList<>();

        Object2LongArrayMap<WeatherEvent> eventByLastTime = new Object2LongArrayMap<>();
        List<WeatherEventInstance> forecast = weatherForecast.getForecast();
        long lastDay = !forecast.isEmpty() ? forecast.get(forecast.size() - 1).scheduledStartTime(dayTime) : currentDay;

        long day = lastCheckedDay;

        for (WeatherEventInstance weatherEventInstance : forecast) {
            eventByLastTime.put(weatherEventInstance.getEvent(this.weatherEvents), weatherEventInstance.scheduledStartTime(dayTime));
        }

        for (; day <= currentDay + this.yearLengthInDays; day++) {
            dayTime += this.dayLength;
            Random random = new Random(world.getSeed() + world.dimension().location().hashCode() + day + seedModifier);
            Collections.shuffle(scrambledKeys, random);
            Season season = seasonContext == null ? null : seasonContext.getSeasonForYearTime(seasonContext.getYearTime());
            for (String key : scrambledKeys) {
                WeatherEvent value = this.weatherEvents.get(key);
                if ((/*day - eventByLastTime.getOrDefault(value, currentDay)) > value.getMinNumberOfNightsBetween() &&*/ (day - lastDay) > this.minDaysBetweenEvents && value.getChance(season) > random.nextDouble())) {
                    lastDay = day;
                    newWeatherEvents.add(new WeatherEventInstance(key, day, world.random.nextInt((int) this.getDayLength()), world.random.nextInt( this.getEventMaxLength() - this.getEventMinLength()) + this.getEventMinLength()));
                    eventByLastTime.put(value, day);
                }
            }
        }
        forecast.addAll(newWeatherEvents);
        weatherForecast.setLastCheckedGameTime(Math.max(day * dayLength, forecast.get(forecast.size() - 1).getEndTime(this.dayLength)));
        return weatherForecast;
    }


    public void tick(World world) {
        WeatherEvent lastEvent = this.currentEvent;
        long dayTime = world.getDayTime();
        long currentDay = (dayTime / this.dayLength);
        if (!world.isClientSide) {
            List<ServerPlayerEntity> players = ((ServerWorld) world).players();
            updateForecast(world, currentDay, players);
            List<WeatherEventInstance> forecast = this.getWeatherForecast().getForecast();
            if (forecast.isEmpty()) {
                this.currentEvent = DEFAULT;
            } else {
                WeatherEventInstance nextEvent = forecast.get(0);
                this.currentEvent = nextEvent.active(dayTime, this.dayLength) ? nextEvent.getEvent(this.weatherEvents) : DEFAULT;
            }

            if (lastEvent != this.currentEvent) {
                NetworkHandler.sendToAllPlayers(players, new WeatherEventChangedPacket(this.currentEvent.getKey()));
            }
            if (!world.getLevelData().isRaining()) {
                if (this.currentEvent != DEFAULT) {
                    world.getLevelData().setRaining(true);
                }
            } else {
                if (this.currentEvent == DEFAULT) {
                    world.getLevelData().setRaining(false);
                }
            }
        }
        this.strength = MathHelper.clamp(this.strength + 0.01F, 0, 1.0F);
    }

    private void updateForecast(World world, long dayTime, List<ServerPlayerEntity> players) {
        updateForecast(world, dayTime);
        long lastCheckedGameTime = this.weatherForecast.getLastCheckedGameTime();
        WeatherForecast newWeatherForecast = computeWeatherForecast((ServerWorld) world, this.weatherForecast);

        long newLastCheckedGameTime = newWeatherForecast.getLastCheckedGameTime();
        long newLastCheckedDay = newLastCheckedGameTime / this.dayLength;
        long lastCheckedDay = lastCheckedGameTime / this.dayLength;
        if (newLastCheckedDay != lastCheckedDay) {
            NetworkHandler.sendToAllPlayers(players, new WeatherForecastChangedPacket(this.weatherForecast));
            NetworkHandler.sendToAllPlayers(((ServerWorld) world).players(), new WeatherEventChangedPacket(this.currentEvent.getKey()));
            WeatherEventSavedData.get(world).setForecast(weatherForecast);
        }
    }

    public void updateForecast(World world, long dayTime) {
        List<WeatherEventInstance> forecast = this.weatherForecast.getForecast();
        if (forecast.isEmpty()) {
            return;
        }

        WeatherEventInstance nextEvent = forecast.get(0);
        if (nextEvent.eventPassed(dayTime, this.dayLength)) {
            this.weatherForecast.getPastEvents().add(nextEvent);
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

    public long getDayLength() {
        return dayLength;
    }

    // TODO: Config
    public int getEventMaxLength() {
        return 50000;
    }

    // TODO: Config
    public int getEventMinLength() {
        return 5000;
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
