package corgitaco.betterweather.util;

import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.common.weather.WeatherContext;

import javax.annotation.Nullable;

public interface BetterWeatherWorldData {

    @Nullable
    SeasonContext getSeasonContext();

    @Nullable
    SeasonContext setSeasonContext(SeasonContext seasonContext);

    @Nullable
    WeatherContext getWeatherEventContext();

    @Nullable
    WeatherContext setWeatherEventContext(WeatherContext weatherEventContext);
}
