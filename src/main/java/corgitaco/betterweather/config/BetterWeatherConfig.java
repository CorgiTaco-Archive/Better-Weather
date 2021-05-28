package corgitaco.betterweather.config;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BetterWeatherConfig {
    public static String COMPATIBILITY_MODE;
    public static List<String> SEASON_DIMENSIONS;
    public static List<String> WEATHER_EVENT_DIMENSIONS;
    public static boolean SERIALIZE_AS_JSON;

    public static void serialize() {
        AbstractCommentedConfigHelper builder = new AbstractCommentedConfigHelper(BetterWeather.CONFIG_PATH.resolve("better-weather.toml"));
        COMPATIBILITY_MODE = builder.add("Whenever, or not to use GLSL Shaders. \n- \"auto\" Automatically disables if: Optifine is installed, or your driver doesn't support OpenGL 2.0 \n- \"force_off\" Forces GLSL Shaders off, will be slower for modern drives. \nDefault: [\"auto\"]", "compatibility_mode", "auto");
        SEASON_DIMENSIONS = builder.addList("What dimensions have seasons?\nMod IDs or dimension IDs ONLY.\nDefault: [\"minecraft:overworld\"]", "season_dimension_whitelist", Collections.singletonList(World.OVERWORLD.getLocation().toString()));
        WEATHER_EVENT_DIMENSIONS = builder.addList("What dimensions have weather events?\nMod IDs or dimension IDs ONLY.\nDefault: [\"minecraft:overworld\"]", "weather_event_dimension_whitelist", Collections.singletonList(World.OVERWORLD.getLocation().toString()));
        SERIALIZE_AS_JSON = builder.add("Serialize configs(besides this one) to json?", "use_json", false);
        builder.build();
    }
}
