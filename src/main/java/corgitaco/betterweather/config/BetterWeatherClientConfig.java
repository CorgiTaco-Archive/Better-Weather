package corgitaco.betterweather.config;

import corgitaco.betterweather.BetterWeather;

public class BetterWeatherClientConfig {
    public final boolean useServerClientSettings;

    public BetterWeatherClientConfig() {
        AbstractCommentedConfigHelper builder = new AbstractCommentedConfigHelper(BetterWeather.CONFIG_PATH.resolve("better-weather-client.toml"));
        this.useServerClientSettings = builder.add("Match client configurations from the server?", "matchServer", false);
        builder.build();
    }
}