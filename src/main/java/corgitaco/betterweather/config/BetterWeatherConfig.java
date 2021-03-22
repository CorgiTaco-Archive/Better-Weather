package corgitaco.betterweather.config;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BetterWeatherConfig {
    public static List<String> SEASON_DIMENSIONS;

    public static void serialize() {
        AbstractCommentedConfigBuilder builder = new AbstractCommentedConfigBuilder(BetterWeather.CONFIG_PATH.resolve("better-weather.toml"));
        SEASON_DIMENSIONS = builder.addList("What dimensions have seasons?", "season_dimension_whitelist", Collections.singletonList(World.OVERWORLD.getLocation().toString()));
        builder.build();
    }
}
