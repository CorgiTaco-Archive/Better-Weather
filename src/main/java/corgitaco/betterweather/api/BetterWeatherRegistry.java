package corgitaco.betterweather.api;

import com.mojang.serialization.Codec;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.mixin.access.RegistryAccess;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class BetterWeatherRegistry {

    // TODO: Make this a registry similar to those of world gen registries.
    public static final Map<ResourceLocation, WeatherEvent> DEFAULT_EVENTS = new HashMap<>();

    public static final RegistryKey<Registry<Codec<? extends WeatherEvent>>> WEATHER_EVENT_KEY = RegistryKey.createRegistryKey(new ResourceLocation(BetterWeather.MOD_ID, "weather_event"));

    public static final RegistryKey<Registry<Codec<? extends WeatherEventClientSettings>>> CLIENT_WEATHER_EVENT_KEY = RegistryKey.createRegistryKey(new ResourceLocation(BetterWeather.MOD_ID, "weather_event_client"));

    public static final Registry<Codec<? extends WeatherEvent>> WEATHER_EVENT = RegistryAccess.invokeRegisterSimple(WEATHER_EVENT_KEY, () -> WeatherEvent.CODEC);

    public static final Registry<Codec<? extends WeatherEventClientSettings>> CLIENT_WEATHER_EVENT_SETTINGS = RegistryAccess.invokeRegisterSimple(CLIENT_WEATHER_EVENT_KEY, () -> WeatherEventClientSettings.CODEC);
}
