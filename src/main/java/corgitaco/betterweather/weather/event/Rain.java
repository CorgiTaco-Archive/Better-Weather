package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;

public class Rain extends WeatherEvent {

    public static final Codec<Rain> CODEC = RecordCodecBuilder.create((builder) -> {
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
        }), Codec.simpleMap(Season.Key.CODEC, Codec.unboundedMap(Season.Phase.CODEC, Codec.DOUBLE), IStringSerializable.createKeyable(Season.Key.values())).fieldOf("seasonChances").forGetter(rain -> {
            return rain.getSeasonChances();
        })).apply(builder, Rain::new);
    });

    public Rain(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, Map<Season.Key, Map<Season.Phase, Double>> seasonChance) {
        super(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, seasonChance);
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
        return TomlCommentedConfigOps.INSTANCE;
    }

    @Override
    public double getTemperatureModifierAtPosition(BlockPos pos) {
        return 0;
    }

    @Override
    public double getHumidityModifierAtPosition(BlockPos pos) {
        return 0;
    }
}
