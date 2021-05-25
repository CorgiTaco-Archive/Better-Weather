package corgitaco.betterweather.helpers;

import corgitaco.betterweather.season.SeasonContext;
import corgitaco.betterweather.weatherevent.BWWeatherEventContext;

import javax.annotation.Nullable;

public interface BetterWeatherWorldData {

    @Nullable
    SeasonContext getSeasonContext();

    @Nullable
    SeasonContext setSeasonContext(SeasonContext seasonContext);

    @Nullable
    BWWeatherEventContext getWeatherEventContext();

    @Nullable
    BWWeatherEventContext setWeatherEventContext(BWWeatherEventContext weatherEventContext);
}
