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
import java.util.HashMap;
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

    public static final Map<String, String> VALUE_COMMENTS = Util.make(new HashMap<>(WeatherEvent.VALUE_COMMENTS), (map) -> {
        map.put("defaultChance", "What is the default chance for this weather event to occur? This value is only used when Seasons are NOT present in the given dimension.");
        map.put("type", "Target Weather Event's Registry ID to configure settings for in this config.");
        map.put("seasonChances", "What is the chance for this weather event to occur for the given season (phase)?");
        map.put("biomeCondition", "Better Weather uses a prefix system for what biomes weather is allowed to function in.\n Prefix Guide:\n \"#\" - Biome category representable.\n \"$\" - Biome dictionary representable.\n \",\" - Creates a new condition, separate from the previous.\n \"ALL\" - Spawn in all biomes(no condition).\n \"!\" - Negates/flips/does the reverse of the condition.\n \"\" - No prefix serves as a biome ID OR Mod ID representable.\n\n Here are a few examples:\n1. \"byg#THE_END, $OCEAN\" would mean that the ore may spawn in biomes with the name space \"byg\" AND in the \"END\" biome category, OR all biomes in the \"OCEAN\" dictionary.\n2. \"byg:guiana_shield, #MESA\" would mean that the ore may spawn in the \"byg:guiana_shield\" OR all biomes in the \"MESA\" category.\n3. \"byg#ICY$MOUNTAIN\" would mean that the ore may only spawn in biomes from byg in the \"ICY\" category and \"MOUNTAIN\" dictionary type.\n4. \"!byg#DESERT\" would mean that the ore may only spawn in biomes that are NOT from byg and NOT in the \"DESERT\" category.\n5. \"ALL\", spawn everywhere. \n6. \"\" Don't spawn anywhere.");
    });

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(VALUE_COMMENTS, true);

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
