package corgitaco.betterweather.api;

import com.mojang.serialization.Codec;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClient;
import corgitaco.betterweather.mixin.access.RegistryAccess;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class BetterWeatherRegistry {

    public static final RegistryKey<Registry<Codec<? extends WeatherEvent>>> WEATHER_EVENT_KEY = RegistryKey.getOrCreateRootKey(new ResourceLocation(BetterWeather.MOD_ID, "weather_event"));

    public static final RegistryKey<Registry<Codec<? extends WeatherEventClient>>> CLIENT_WEATHER_EVENT_KEY = RegistryKey.getOrCreateRootKey(new ResourceLocation(BetterWeather.MOD_ID, "weather_event_client"));

    public static final Registry<Codec<? extends WeatherEvent>> WEATHER_EVENT = RegistryAccess.invokeCreateRegistry(WEATHER_EVENT_KEY, () -> {
        return WeatherEvent.CODEC;
    });

    public static final Registry<Codec<? extends WeatherEventClient>> CLIENT_WEATHER_EVENT = RegistryAccess.invokeCreateRegistry(CLIENT_WEATHER_EVENT_KEY, () -> {
        return WeatherEventClient.CODEC;
    });
}
