package corgitaco.betterweather.helpers;

import corgitaco.betterweather.season.SeasonContext;

import javax.annotation.Nullable;

public interface BetterWeatherWorldData {

    @Nullable
    SeasonContext getSeasonContext();

    @Nullable
    SeasonContext setSeasonContext(SeasonContext seasonContext);
}
