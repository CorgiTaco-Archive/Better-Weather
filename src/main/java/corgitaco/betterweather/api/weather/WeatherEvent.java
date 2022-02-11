package corgitaco.betterweather.api.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.season.Season;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public abstract class WeatherEvent implements WeatherEventSettings {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final boolean MODIFY_TEMPERATURE = false;

    public static final Codec<WeatherEvent> CODEC = BetterWeatherRegistry.WEATHER_EVENT.dispatchStable(WeatherEvent::codec, Function.identity());

    public static final Map<Season.Key, Map<Season.Phase, Double>> NO_SEASON_CHANCES = Util.make(new IdentityHashMap<>(), (map) -> {
        for (Season.Key value : Season.Key.values()) {
            Object2DoubleOpenHashMap<Season.Phase> phaseDoubleMap = new Object2DoubleOpenHashMap<>();
            for (Season.Phase phase : Season.Phase.values()) {
                phaseDoubleMap.put(phase, 0.0D);
            }
            map.put(value, phaseDoubleMap);
        }
    });

    public static final Map<String, String> VALUE_COMMENTS = Util.make(new HashMap<>(WeatherEventClientSettings.VALUE_COMMENTS), (map) -> {
        map.put("defaultChance", "What is the default chance for this weather event to occur? This value is only used when Seasons are NOT present in the given dimension.");
        map.put("temperatureOffset", "What is the temperature offset for valid biomes?");
        map.put("humidityOffset", "What is the temperature offset for valid biomes?");
        map.put("isThundering", "Determines whether or not this weather event may spawn lightning and sets world info internally for MC and mods to use.");
        map.put("lightningChance", "How often does lightning spawn? Requires \"isThundering\" to be true.");
        map.put("type", "Target Weather Event's Registry ID to configure settings for in this config.");
        map.put("seasonChances", "What is the chance for this weather event to occur for the given season (phase)?");
        map.put("biomeCondition", "Better Weather uses a prefix system for what biomes weather is allowed to function in.\n Prefix Guide:\n \"#\" - Biome category representable.\n \"$\" - Biome dictionary representable.\n \",\" - Creates a new condition, separate from the previous.\n \"ALL\" - Spawn in all biomes(no condition).\n \"!\" - Negates/flips/does the reverse of the condition.\n \"\" - No prefix serves as a biome ID OR Mod ID representable.\n\n Here are a few examples:\n1. \"byg#THE_END, $OCEAN\" would mean that the ore may spawn in biomes with the name space \"byg\" AND in the \"END\" biome category, OR all biomes in the \"OCEAN\" dictionary.\n2. \"byg:guiana_shield, #MESA\" would mean that the ore may spawn in the \"byg:guiana_shield\" OR all biomes in the \"MESA\" category.\n3. \"byg#ICY$MOUNTAIN\" would mean that the ore may only spawn in biomes from byg in the \"ICY\" category and \"MOUNTAIN\" dictionary type.\n4. \"!byg#DESERT\" would mean that the ore may only spawn in biomes that are NOT from byg and NOT in the \"DESERT\" category.\n5. \"ALL\", spawn everywhere. \n6. \"\" Don't spawn anywhere.");
    });

    private final String biomeCondition;
    private final double defaultChance;
    private final double temperatureOffsetRaw;
    private final double humidityOffsetRaw;
    private final boolean isThundering;
    private final int lightningFrequency;
    private final Map<Season.Key, Map<Season.Phase, Double>> seasonChances;
    private final ReferenceArraySet<Biome> validBiomes = new ReferenceArraySet<>();
    private WeatherEventClientSettings clientSettings;
    private WeatherEventClient<?> client;
    private String key;

    public WeatherEvent(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, boolean isThundering, int lightningFrequency, Map<Season.Key, Map<Season.Phase, Double>> seasonChance) {
        this.clientSettings = clientSettings;
        this.biomeCondition = biomeCondition;
        this.defaultChance = defaultChance;
        this.temperatureOffsetRaw = temperatureOffsetRaw;
        this.humidityOffsetRaw = humidityOffsetRaw;
        this.isThundering = isThundering;
        this.lightningFrequency = lightningFrequency;
        this.seasonChances = seasonChance;
    }

    public String getKey() {
        return key;
    }

    public WeatherEvent setKey(String key) {
        this.key = key;
        return this;
    }

    public final double getChance(@Nullable Season season) {
        return season != null ? this.seasonChances.get(season.getKey()).get(season.getPhase()) : this.defaultChance;
    }

    public final double getDefaultChance() {
        return defaultChance;
    }

    public final Map<Season.Key, Map<Season.Phase, Double>> getSeasonChances() {
        return seasonChances;
    }

    public abstract void worldTick(ServerWorld world, int tickSpeed, long worldTime);

    public abstract Codec<? extends WeatherEvent> codec();

    public abstract DynamicOps<?> configOps();

    public void livingEntityUpdate(LivingEntity entity) {
    }

    /**
     * This is called in the chunk ticking iterator.
     */
    public void chunkTick(Chunk chunk, ServerWorld world) {
    }

    public final void doChunkTick(Chunk chunk, ServerWorld world) {
        chunkTick(chunk, world);
    }

    public void onChunkLoad(Chunk chunk, ServerWorld world) {

    }

    public final void doChunkLoad(Chunk chunk, ServerWorld world) {
        onChunkLoad(chunk, world);
    }

    public boolean fillBlocksWithWater() {
        return false;
    }

    public boolean spawnSnowInFreezingClimates() {
        return true;
    }

    public final TranslationTextComponent successTranslationTextComponent(String key) {
        return new TranslationTextComponent("commands.bw.setweather.success",
                new TranslationTextComponent("bw.weather." + key));
    }

    public void fillBiomes(Registry<Biome> biomeRegistry) {
        Set<Map.Entry<RegistryKey<Biome>, Biome>> entries = biomeRegistry.entrySet();

        for (Map.Entry<RegistryKey<Biome>, Biome> entry : entries) {
            Biome biome = entry.getValue();
            RegistryKey<Biome> key = entry.getKey();

            if (conditionPasses(this.biomeCondition, key, biome)) {
                this.validBiomes.add(biome);
            }
        }
    }

    public WeatherEventClientSettings getClientSettings() {
        return clientSettings;
    }

    @OnlyIn(Dist.CLIENT)
    public WeatherEvent setClientSettings(WeatherEventClientSettings clientSettings) {
        this.clientSettings = clientSettings;
        return this;
    }

    public String getBiomeCondition() {
        return biomeCondition;
    }

    public boolean isValidBiome(Biome biome) {
        return this.validBiomes.contains(biome);
    }

    public static boolean conditionPasses(String conditionString, RegistryKey<Biome> biomeKey, Biome biome) {
        if (conditionString.isEmpty()) {
            return false;
        }

        if (conditionString.equalsIgnoreCase("all")) {
            return true;
        }

        String[] conditions = conditionString.trim().split("\\s*,\\s*");
        String biomeNamespace = biomeKey.location().getNamespace();
        String biomeLocation = biomeKey.location().toString();
        for (String condition : conditions) {
            String[] split = condition.split("(?=[\\$#])");
            boolean categoryExists = true;
            for (String result : split) {
                if (result.equals("!")) {
                    continue;
                }

                if (result.startsWith("#")) {
                    String categoryString = result.substring(1);
                    categoryExists = Arrays.stream(Biome.Category.values()).anyMatch(bc -> bc.toString().equalsIgnoreCase(categoryString));
                    if (!categoryExists) {
                        LOGGER.error("\"" + categoryString + "\" is not a valid biome category!");
                    }
                }
            }
            if (!categoryExists) {
                continue;
            }
            int passes = 0;
            for (String result : split) {
                if (result.equals("!")) {
                    continue;
                }
                if (result.startsWith("!")) {
                    result = result.substring(1);
                }
                if (result.startsWith("$")) {
                    if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.getType(result.substring(1).toUpperCase()))) {
                        passes++;
                    }
                } else if (result.startsWith("#")) {
                    String categoryString = result.substring(1);
                    if (biome.getBiomeCategory().getName().equalsIgnoreCase(categoryString)) {
                        passes++;
                    }
                } else if (biomeLocation.equalsIgnoreCase(result) && result.equalsIgnoreCase(biomeNamespace)) {
                    passes++;
                }
            }
            boolean isFlipped = condition.startsWith("!");
            if (passes == 0) {
                if (isFlipped) {
                    return true;
                }
            }

            if (passes > 0 && !isFlipped) {
                return true;
            }
        }
        return false;
    }

    public double getTemperatureOffsetRaw() {
        return temperatureOffsetRaw;
    }

    public double getHumidityOffsetRaw() {
        return humidityOffsetRaw;
    }

    public boolean isThundering() {
        return isThundering;
    }

    public int getLightningChance() {
        return lightningFrequency;
    }

    @OnlyIn(Dist.CLIENT)
    public WeatherEventClient<?> getClient() {
        return client;
    }

    @OnlyIn(Dist.CLIENT)
    public WeatherEvent setClient(WeatherEventClient<?> client, String path) {
        if (client == null) {
            throw new NullPointerException("Client settings for file \"" + path + "\" are incomplete/broken. The fastest solution is to copy this file to a separate directory and update the settings in the new file for all relevant fields.");
        }
        this.client = client;
        return this;
    }
}