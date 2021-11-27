package corgitaco.betterweather.config;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BetterWeatherConfig {
    public static String COMPATIBILITY_MODE = "auto";
    public static List<String> SEASON_DIMENSIONS;
    public static List<String> WEATHER_EVENT_DIMENSIONS;
    public static boolean SERIALIZE_AS_JSON;
    public static boolean FORCE_PER_WORLD_REGISTRY;

    public static void serialize() {
        AbstractCommentedConfigHelper builder = new AbstractCommentedConfigHelper(BetterWeather.CONFIG_PATH.resolve("better-weather.toml"));
//        COMPATIBILITY_MODE = builder.add("Whenever, or not to use GLSL Shaders. \n- \"auto\" Automatically disables if: Optifine is installed, or your driver doesn't support OpenGL 2.0 \n- \"force_off\" Forces GLSL Shaders off, will be slower for modern drives. \nDefault: [\"auto\"]", "compatibility_mode", "auto");
        SEASON_DIMENSIONS = builder.addList("What dimensions have seasons?\nDimension IDs ONLY.\nWARNING: If the number of listed season dimensions exceeds 1 or if the listed season & weather dimension(s) do not match, the dimensions listed will use a per dimension gen data pack registry, this may have unintended side effects like mod incompatibilities.\nDefault: [\"minecraft:overworld\"]", "seasonDimensionWhitelist", Collections.singletonList(World.OVERWORLD.location().toString()));
        WEATHER_EVENT_DIMENSIONS = builder.addList("What dimensions have weather events?\nDimension IDs ONLY.\nWARNING: If the number of listed weather dimensions exceeds 1 or if the listed weather & season dimension(s) do not match, the dimensions listed will use a per dimension world gen data pack registry, this may have unintended side effects like mod incompatibilities.\nDefault: [\"minecraft:overworld\"]", "weatherEventDimensionWhitelist", Collections.singletonList(World.OVERWORLD.location().toString()));
        SERIALIZE_AS_JSON = builder.add("Serialize configs(besides this one) to json?", "useJsonSerializer", false);
        FORCE_PER_WORLD_REGISTRY = builder.add("Force per dimension world gen data pack registries?\nWARNING: This will force a per dimension world gen data pack registry, this may have unintended side effects like mod incompatibilities.", "forcePerDimensionRegistry", false);
        builder.build();
    }
}
