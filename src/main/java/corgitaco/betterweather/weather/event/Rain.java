package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import corgitaco.betterweather.util.client.ColorUtil;
import corgitaco.betterweather.weather.event.client.settings.RainClientSettings;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumMap;
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
        }), Codec.BOOL.fieldOf("isThundering").forGetter(rain -> {
            return rain.isThundering();
        }), Codec.INT.fieldOf("lightningChance").forGetter(rain -> {
            return rain.getLightningChance();
        }), Codec.simpleMap(Season.Key.CODEC, Codec.unboundedMap(Season.Phase.CODEC, Codec.DOUBLE), IStringSerializable.createKeyable(Season.Key.values())).fieldOf("seasonChances").forGetter(rain -> {
            return rain.getSeasonChances();
        })).apply(builder, Rain::new);
    });

    public static final ResourceLocation RAIN_LOCATION = new ResourceLocation("minecraft:textures/environment/rain.png");
    public static final ResourceLocation SNOW_LOCATION = new ResourceLocation("minecraft:textures/environment/snow.png");

    public static final ColorSettings THUNDER_COLORS = new ColorSettings(Integer.MAX_VALUE, 0.1, Integer.MAX_VALUE, 0.0, ColorUtil.DEFAULT_THUNDER_SKY, 1.0F, ColorUtil.DEFAULT_THUNDER_FOG, 1.0F, ColorUtil.DEFAULT_THUNDER_CLOUDS, 1.0F);
    public static final ColorSettings RAIN_COLORS = new ColorSettings(Integer.MAX_VALUE, 0.1, Integer.MAX_VALUE, 0.0, ColorUtil.DEFAULT_RAIN_SKY, 1.0F, ColorUtil.DEFAULT_RAIN_FOG, 1.0F, ColorUtil.DEFAULT_RAIN_CLOUDS, 1.0F);

    public static final String DEFAULT_BIOME_CONDITION = "!#DESERT#SAVANNA#NETHER#THEEND";

    public static final Rain DEFAULT = new Rain(new RainClientSettings(RAIN_COLORS, 0.0F, -1.0F, true, RAIN_LOCATION, SNOW_LOCATION), DEFAULT_BIOME_CONDITION, 0.7D, -0.1, 0.1, false, 0,
            Util.make(new EnumMap<>(Season.Key.class), (seasons) -> {
                seasons.put(Season.Key.SPRING, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.7);
                    phases.put(Season.Phase.MID, 0.8);
                    phases.put(Season.Phase.END, 0.5);
                }));

                seasons.put(Season.Key.SUMMER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.1);
                    phases.put(Season.Phase.MID, 0.0);
                    phases.put(Season.Phase.END, 0.0);
                }));

                seasons.put(Season.Key.AUTUMN, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.1);
                    phases.put(Season.Phase.MID, 0.1);
                    phases.put(Season.Phase.END, 0.1);
                }));

                seasons.put(Season.Key.WINTER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.1);
                    phases.put(Season.Phase.MID, 0.1);
                    phases.put(Season.Phase.END, 0.2);
                }));
            }));

    public static final Rain DEFAULT_THUNDERING = new Rain(new RainClientSettings(THUNDER_COLORS, 0.0F, -1.0F, true, RAIN_LOCATION, SNOW_LOCATION), DEFAULT_BIOME_CONDITION, 0.3D, -0.5, 0.1, true, 100000,
            Util.make(new EnumMap<>(Season.Key.class), (seasons) -> {
                seasons.put(Season.Key.SPRING, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.35);
                    phases.put(Season.Phase.MID, 0.4);
                    phases.put(Season.Phase.END, 0.25);
                }));

                seasons.put(Season.Key.SUMMER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.05);
                    phases.put(Season.Phase.MID, 0.0);
                    phases.put(Season.Phase.END, 0.0);
                }));

                seasons.put(Season.Key.AUTUMN, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.05);
                    phases.put(Season.Phase.MID, 0.05);
                    phases.put(Season.Phase.END, 0.05);
                }));

                seasons.put(Season.Key.WINTER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.05);
                    phases.put(Season.Phase.MID, 0.05);
                    phases.put(Season.Phase.END, 0.1);
                }));
            }));

    public Rain(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, boolean isThundering, int lightningFrequency, Map<Season.Key, Map<Season.Phase, Double>> seasonChance) {
        super(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, isThundering, lightningFrequency, seasonChance);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {
    }

    @Override
    public void chunkTick(Chunk chunk, ServerWorld world) {
        super.chunkTick(chunk, world);
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
        return getTemperatureOffsetRaw();
    }

    @Override
    public double getHumidityModifierAtPosition(BlockPos pos) {
        return getHumidityOffsetRaw();
    }
}
