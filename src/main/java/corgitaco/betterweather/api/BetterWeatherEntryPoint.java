package corgitaco.betterweather.api;

import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Where Better Weather collects its values.
 */
public class BetterWeatherEntryPoint {
    public static final ObjectOpenHashSet<WeatherEvent> WEATHER_EVENTS = new ObjectOpenHashSet<>();

}
