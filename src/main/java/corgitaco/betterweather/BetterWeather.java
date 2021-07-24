package corgitaco.betterweather;

import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.config.BetterWeatherClientConfig;
import corgitaco.betterweather.weather.event.*;
import corgitaco.betterweather.weather.event.client.settings.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class BetterWeather {
    public static final String MOD_ID = "betterweather";
    public static final Path CONFIG_PATH = new File(String.valueOf(FMLPaths.CONFIGDIR.get().resolve(MOD_ID))).toPath();
    public static final Logger LOGGER = LogManager.getLogger();

    public static final BetterWeatherClientConfig CLIENT_CONFIG = new BetterWeatherClientConfig();

    public BetterWeather() {
        if (!CONFIG_PATH.toFile().exists())
            CONFIG_PATH.toFile().mkdir();
    }

    public static void registerDefaultEvents() {
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "none"), None.DEFAULT);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "acid_rain"), AcidRain.DEFAULT);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "blizzard"), Blizzard.DEFAULT);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "cloudy"), Cloudy.DEFAULT);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "rain"), Rain.DEFAULT);

        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "acid_rain_thundering"), AcidRain.DEFAULT_THUNDERING);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_thundering"), Blizzard.DEFAULT_THUNDERING);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "cloudy_thundering"), Cloudy.DEFAULT_THUNDERING);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "thundering"), Rain.DEFAULT_THUNDERING);
    }

    public static void registerWeatherEventCodecs() {
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "acid_rain"), AcidRainClientSettings.CODEC);
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "blizzard"), BlizzardClientSettings.CODEC);
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "cloudy"), CloudyClientSettings.CODEC);
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "none"), NoneClientSettings.CODEC);
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "rain"), RainClientSettings.CODEC);

        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "acid_rain"), AcidRain.CODEC);
        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "blizzard"), Blizzard.CODEC);
        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "cloudy"), Cloudy.CODEC);
        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "none"), None.CODEC);
        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "rain"), Rain.CODEC);
    }
}
