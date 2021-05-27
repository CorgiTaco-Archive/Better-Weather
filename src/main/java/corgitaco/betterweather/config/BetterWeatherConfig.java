package corgitaco.betterweather.config;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BetterWeatherConfig {
    public static List<String> SEASON_DIMENSIONS;
    public static List<String> WEATHER_EVENT_DIMENSIONS;
    public static boolean SERIALIZE_AS_JSON;

    public static void serialize() {
        AbstractCommentedConfigHelper builder = new AbstractCommentedConfigHelper(BetterWeather.CONFIG_PATH.resolve("better-weather.toml"));
        SEASON_DIMENSIONS = builder.addList("What dimensions have seasons?\nMod IDs or dimension IDs ONLY.\nDefault: [\"minecraft:overworld\"]", "season_dimension_whitelist", Collections.singletonList(World.OVERWORLD.getLocation().toString()));
        WEATHER_EVENT_DIMENSIONS = builder.addList("What dimensions have weather events?\nMod IDs or dimension IDs ONLY.\nDefault: [\"minecraft:overworld\"]", "weather_event_dimension_whitelist", Collections.singletonList(World.OVERWORLD.getLocation().toString()));
        SERIALIZE_AS_JSON = builder.add("Serialize configs(besides this one) to json?", "use_json", false);
        builder.build();
    }
}
