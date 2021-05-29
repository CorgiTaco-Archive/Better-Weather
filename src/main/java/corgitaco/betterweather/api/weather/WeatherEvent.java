package corgitaco.betterweather.api.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.core.SoundRegistry;
import corgitaco.betterweather.graphics.Graphics;
import corgitaco.betterweather.season.client.ColorSettings;
import corgitaco.betterweather.weather.event.Blizzard;
import corgitaco.betterweather.weather.event.None;
import corgitaco.betterweather.weather.event.client.BlizzardClientSettings;
import corgitaco.betterweather.weather.event.client.NoneClientSettings;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;

import java.util.*;
import java.util.function.Function;

public abstract class WeatherEvent implements WeatherEventSettings {

    public static final Codec<WeatherEvent> CODEC = BetterWeatherRegistry.WEATHER_EVENT.dispatchStable(WeatherEvent::codec, Function.identity());

    public static final Map<Season.Key, Map<Season.Phase, Double>> NO_SEASON_CHANCES = Util.make(new IdentityHashMap<>(), (map) -> {
        for (Season.Key value : Season.Key.values()) {
            IdentityHashMap<Season.Phase, Double> phaseDoubleMap = new IdentityHashMap<>();
            for (Season.Phase phase : Season.Phase.values()) {
                phaseDoubleMap.put(phase, 0.0D);
            }
            map.put(value, phaseDoubleMap);
        }
    });

    public static final Map<String, String> VALUE_COMMENTS = Util.make(new HashMap<>(ColorSettings.VALUE_COMMENTS), (map) -> {
        map.put("defaultChance", "What is the default chance for this weather event to occur? This value is only used when Seasons are NOT present in the given dimension.");
        map.put("type", "Target Weather Event's Registry ID to configure settings for in this config.");
        map.put("seasonChances", "What is the chance for this weather event to occur for the given season (phase)?");
        map.put("biomeCondition", "Better Weather uses a prefix system for what biomes weather is allowed to function in.\n Prefix Guide:\n \"#\" - Biome category representable.\n \"$\" - Biome dictionary representable.\n \",\" - Creates a new condition, separate from the previous.\n \"ALL\" - Spawn in all biomes(no condition).\n \"!\" - Negates/flips/does the reverse of the condition.\n \"\" - No prefix serves as a biome ID OR Mod ID representable.\n\n Here are a few examples:\n1. \"byg#THE_END, $OCEAN\" would mean that the ore may spawn in biomes with the name space \"byg\" AND in the \"END\" biome category, OR all biomes in the \"OCEAN\" dictionary.\n2. \"byg:guiana_shield, #MESA\" would mean that the ore may spawn in the \"byg:guiana_shield\" OR all biomes in the \"MESA\" category.\n3. \"byg#ICY$MOUNTAIN\" would mean that the ore may only spawn in biomes from byg in the \"ICY\" category and \"MOUNTAIN\" dictionary type.\n4. \"!byg#DESERT\" would mean that the ore may only spawn in biomes that are NOT from byg and NOT in the \"DESERT\" category.\n5. \"ALL\", spawn everywhere. \n6. \"\" Don't spawn anywhere.");
    });

    public static final None NONE = new None(new NoneClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0)));
    public static final Blizzard BLIZZARD = new Blizzard(new BlizzardClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0), new ResourceLocation("minecraft:textures/environment/snow.png"), SoundRegistry.BLIZZARD_LOOP2, 100, 1.0F, 1.0F), "!#DESERT#SAVANNA", 0.0D, NO_SEASON_CHANCES);

    public static final Set<WeatherEvent> DEFAULT_EVENTS = Util.make(new ReferenceArraySet<>(), (set) -> {
        set.add(BLIZZARD);
        set.add(NONE);
    });

    private final WeatherEventClientSettings clientSettings;
    private final String biomeCondition;
    private final double defaultChance;
    private final Map<Season.Key, Map<Season.Phase, Double>> seasonChances;

    private String name;
    private final ReferenceArraySet<Biome> validBiomes = new ReferenceArraySet<>();

    public WeatherEvent(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, Map<Season.Key, Map<Season.Phase, Double>> seasonChance) {
        this.clientSettings = clientSettings;
        this.biomeCondition = biomeCondition;
        this.defaultChance = defaultChance;
        this.seasonChances = seasonChance;
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

    public void livingEntityUpdate(Entity entity) {
    }

    public boolean weatherParticlesAndSound(ActiveRenderInfo renderInfo, Minecraft mc) {
        return true;
    }

    /**
     * This is called in the chunk ticking iterator.
     */
    public void tickLiveChunks(Chunk chunk, ServerWorld world) {
    }

    public boolean fillBlocksWithWater() {
        return false;
    }

    public boolean spawnSnowInFreezingClimates() {
        return false;
    }

    public final TranslationTextComponent successTranslationTextComponent() {
        return new TranslationTextComponent("commands.bw.setweather.success." + BetterWeatherRegistry.WEATHER_EVENT.getKey(codec()).toString());
    }

    public WeatherEvent setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void fillBiomes(Registry<Biome> biomeRegistry) {
        Set<Map.Entry<RegistryKey<Biome>, Biome>> entries = biomeRegistry.getEntries();

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

    public String getBiomeCondition() {
        return biomeCondition;
    }

    public boolean isValidBiome(Biome biome) {
        return this.validBiomes.contains(biome);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean renderWeather(Graphics graphics, Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return this.clientSettings.renderWeather(graphics, mc, world, lightTexture, ticks, partialTicks, x, y, z, (this::isValidBiome));
    }

    @OnlyIn(Dist.CLIENT)
    public float skyOpacity(ClientWorld world, BlockPos playerPos) {
        int transitionStart = 12;

        float defaultFogStrength = 0.0F;
        defaultFogStrength *= defaultFogStrength;

        int x = playerPos.getX();
        int z = playerPos.getZ();
        float accumulatedFogStrength = 0.0F;

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int sampleX = x - transitionStart; sampleX <= x + transitionStart; ++sampleX) {
            pos.setX(sampleX);

            for (int sampleZ = z - transitionStart; sampleZ <= z + transitionStart; ++sampleZ) {
                pos.setZ(sampleZ);

                Biome biome = world.getBiome(pos);
                if (validBiomes.contains(biome)) {

                    float fogStrength = 2;

                    accumulatedFogStrength += fogStrength * fogStrength;
                } else {
                    accumulatedFogStrength += defaultFogStrength;
                }
            }
        }
        float transitionSmoothness = 33 * 33;
        return Math.min(this.clientSettings.skyOpacity(), (float) Math.sqrt(accumulatedFogStrength / transitionSmoothness));
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {
        this.clientSettings.clientTick(world, tickSpeed, worldTime, mc, this::isValidBiome);
    }


    public static boolean conditionPasses(String conditionString, RegistryKey<Biome> biomeKey, Biome biome) {
        if (conditionString.isEmpty()) {
            return false;
        }

        if (conditionString.equalsIgnoreCase("all")) {
            return true;
        }

        String[] conditions = conditionString.trim().split("\\s*,\\s*");
        String biomeNamespace = biomeKey.getLocation().getNamespace();
        String biomeLocation = biomeKey.getLocation().toString();
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
                        BetterWeather.LOGGER.error("\"" + categoryString + "\" is not a valid biome category!");
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
                    if (biome.getCategory().getName().equalsIgnoreCase(categoryString)) {
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
}