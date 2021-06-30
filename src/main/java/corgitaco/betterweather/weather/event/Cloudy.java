package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class Cloudy extends WeatherEvent {

    public static final Codec<Cloudy> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(WeatherEventClientSettings.CODEC.fieldOf("clientSettings").forGetter((rain) -> {
            return rain.getClientSettings();
        }), Codec.STRING.fieldOf("biomeCondition").forGetter(rain -> {
            return rain.getBiomeCondition();
        }), Codec.DOUBLE.fieldOf("defaultChance").forGetter(rain -> {
            return rain.getDefaultChance();
        }), Codec.DOUBLE.fieldOf("temperatureOffset").forGetter(rain -> {
            return rain.getTemperatureOffsetRaw();
        }), Codec.DOUBLE.fieldOf("humidityOffset").forGetter(rain -> {
            return rain.getHumidityOffsetRaw();
        }), Codec.BOOL.fieldOf("isThundering").forGetter(rain -> {
            return rain.isThundering();
        }), Codec.INT.fieldOf("lightningChance").forGetter(rain -> {
            return rain.getLightningChance();
        }), Codec.simpleMap(Season.Key.CODEC, Codec.unboundedMap(Season.Phase.CODEC, Codec.DOUBLE), IStringSerializable.createKeyable(Season.Key.values())).fieldOf("seasonChances").forGetter(blizzard -> {
            return blizzard.getSeasonChances();
        })).apply(builder, Cloudy::new);
    });

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(WeatherEvent.VALUE_COMMENTS), (map) -> {
    }), true);


    public Cloudy(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, boolean isThundering, int lightningFrequency, Map<Season.Key, Map<Season.Phase, Double>> map) {
        super(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, isThundering, lightningFrequency, NO_SEASON_CHANCES);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {
    }

    @Override
    public Codec<? extends WeatherEvent> codec() {
        return CODEC;
    }

    @Override
    public DynamicOps<?> configOps() {
        return CONFIG_OPS;
    }

    @Override
    public double getTemperatureModifierAtPosition(BlockPos pos) {
        return getTemperatureOffsetRaw();
    }

    @Override
    public double getHumidityModifierAtPosition(BlockPos pos) {
        return getHumidityOffsetRaw();
    }
}
