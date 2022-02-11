package corgitaco.betterweather.config;

import corgitaco.betterweather.BetterWeather;

public class BetterWeatherClientConfig {
    public static BetterWeatherClientConfig INSTANCE = null;

    public final boolean useServerClientSettings;

    public BetterWeatherClientConfig() {
        CommentedConfigBuilder builder = new CommentedConfigBuilder(BetterWeather.CONFIG_PATH.resolve("better-weather-client.toml"));
        this.useServerClientSettings = builder.add("Match client configurations from the server?", "matchServer", false);
        builder.build();
    }

    public static BetterWeatherClientConfig getConfig() {
        return getConfig(false);
    }

    public static BetterWeatherClientConfig getConfig(boolean serialize) {
        if (INSTANCE == null || serialize) {
            INSTANCE = new BetterWeatherClientConfig();
        }
        return INSTANCE;
    }
}