package corgitaco.betterweather.config;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BetterWeatherConfig {
    public static BetterWeatherConfig INSTANCE = null;

    public List<String> seasonDimensions;
    public List<String> weatherEventDimensions;
    public boolean serializeAsJson;
    public boolean forcePerWorldRegistry;

    public BetterWeatherConfig() {
        CommentedConfigBuilder builder = new CommentedConfigBuilder(BetterWeather.CONFIG_PATH.resolve("better-weather.toml"));
        seasonDimensions = builder.addList("What dimensions have seasons?\nDimension IDs ONLY.\nWARNING: If the number of listed season dimensions exceeds 1 or if the listed season & weather dimension(s) do not match, the dimensions listed will use a per dimension gen data pack registry, this may have unintended side effects like mod incompatibilities.\nDefault: [\"minecraft:overworld\"]", "seasonDimensionWhitelist", Collections.singletonList(World.OVERWORLD.location().toString()));
        weatherEventDimensions = builder.addList("What dimensions have weather events?\nDimension IDs ONLY.\nWARNING: If the number of listed weather dimensions exceeds 1 or if the listed weather & season dimension(s) do not match, the dimensions listed will use a per dimension world gen data pack registry, this may have unintended side effects like mod incompatibilities.\nDefault: [\"minecraft:overworld\"]", "weatherEventDimensionWhitelist", Collections.singletonList(World.OVERWORLD.location().toString()));
        serializeAsJson = builder.add("Serialize configs(besides this one) to json?", "useJsonSerializer", false);
        forcePerWorldRegistry = builder.add("Force per dimension world gen data pack registries?\nWARNING: This will force a per dimension world gen data pack registry, this may have unintended side effects like mod incompatibilities.", "forcePerDimensionRegistry", false);
        builder.build();
    }

    public static BetterWeatherConfig getConfig() {
        return getConfig(false);
    }

    public static BetterWeatherConfig getConfig(boolean serialize) {
        if (INSTANCE == null || serialize) {
            INSTANCE = new BetterWeatherConfig();
        }
        return INSTANCE;
    }
}