package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import corgitaco.betterweather.weather.event.client.settings.CloudyClientSettings;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumMap;
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

    public static final Cloudy DEFAULT = new Cloudy(new CloudyClientSettings(Rain.RAIN_COLORS, 0.0F, -1.0F, true), "ALL", 0.7D, !MODIFY_TEMPERATURE ? 0.0 : -0.05, 0.07, false, 0,
            Util.make(new EnumMap<>(Season.Key.class), (map) -> {
                for (Season.Key value : Season.Key.values()) {
                    Map<Season.Phase, Double> phaseDoubleMap = new EnumMap<>(Season.Phase.class);
                    for (Season.Phase phase : Season.Phase.values()) {
                        phaseDoubleMap.put(phase, 0.3D);
                    }
                    map.put(value, phaseDoubleMap);
                }
            }));

    public static final Cloudy DEFAULT_THUNDERING = new Cloudy(new CloudyClientSettings(Rain.THUNDER_COLORS, 0.0F, -0.09F, true), "ALL", 0.1D, !MODIFY_TEMPERATURE ? 0.0 :-0.05, 0.07, true, 100000,
            Util.make(new EnumMap<>(Season.Key.class), (map) -> {
                for (Season.Key value : Season.Key.values()) {
                    Map<Season.Phase, Double> phaseDoubleMap = new EnumMap<>(Season.Phase.class);
                    for (Season.Phase phase : Season.Phase.values()) {
                        phaseDoubleMap.put(phase, 0.1D);
                    }
                    map.put(value, phaseDoubleMap);
                }
            }));

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
