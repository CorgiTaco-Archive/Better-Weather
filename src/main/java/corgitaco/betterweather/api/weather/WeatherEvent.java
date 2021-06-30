package corgitaco.betterweather.api.weather;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.core.SoundRegistry;
import corgitaco.betterweather.graphics.Graphics;
import corgitaco.betterweather.mixin.access.ServerWorldAccess;
import corgitaco.betterweather.season.client.ColorSettings;
import corgitaco.betterweather.util.client.ColorUtil;
import corgitaco.betterweather.weather.event.*;
import corgitaco.betterweather.weather.event.client.BlizzardClientSettings;
import corgitaco.betterweather.weather.event.client.CloudyClientSettings;
import corgitaco.betterweather.weather.event.client.NoneClientSettings;
import corgitaco.betterweather.weather.event.client.RainClientSettings;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
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

    public static final IdentityHashMap<ResourceLocation, ResourceLocation> ACID_RAIN_DECAYER = Util.make(new IdentityHashMap<>(), (map) -> {
        for (Block block : Registry.BLOCK) {
            Material material = block.getDefaultState().getMaterial();
            if (material == Material.LEAVES || material == Material.PLANTS) {
                map.put(Registry.BLOCK.getKey(block), (Registry.BLOCK.getKey(Blocks.AIR)));
            }
        }
        map.put(Registry.BLOCK.getKey(Blocks.GRASS_BLOCK), Registry.BLOCK.getKey(Blocks.DIRT));
        map.put(Registry.BLOCK.getKey(Blocks.PODZOL), Registry.BLOCK.getKey(Blocks.DIRT));
        map.put(Registry.BLOCK.getKey(Blocks.MYCELIUM), Registry.BLOCK.getKey(Blocks.DIRT));
    });

    public static final ColorSettings THUNDER_COLORS = new ColorSettings(Integer.MAX_VALUE, 0.1, Integer.MAX_VALUE, 0.0, ColorUtil.DEFAULT_THUNDER_SKY, 1.0F, ColorUtil.DEFAULT_THUNDER_FOG, 1.0F, ColorUtil.DEFAULT_THUNDER_CLOUDS, 1.0F);
    public static final ColorSettings RAIN_COLORS = new ColorSettings(Integer.MAX_VALUE, 0.1, Integer.MAX_VALUE, 0.0, ColorUtil.DEFAULT_RAIN_SKY, 1.0F, ColorUtil.DEFAULT_RAIN_FOG, 1.0F, ColorUtil.DEFAULT_RAIN_CLOUDS, 1.0F);

    public static final ResourceLocation RAIN_LOCATION = new ResourceLocation("minecraft:textures/environment/rain.png");
    public static final ResourceLocation SNOW_LOCATION = new ResourceLocation("minecraft:textures/environment/snow.png");
    public static final ResourceLocation ACID_RAIN_LOCATION = new ResourceLocation(BetterWeather.MOD_ID, "textures/environment/acid_rain.png");

    public static final None NONE = new None(new NoneClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0)));
    public static final Rain ACID_RAIN = new AcidRain(new RainClientSettings(RAIN_COLORS, 0.0F, -1.0F, true, ACID_RAIN_LOCATION, SNOW_LOCATION), "!#DESERT#SAVANNA", 0.25D, -0.1, 0.1, 16, 8, ACID_RAIN_DECAYER, false, 0,
            Util.make(new EnumMap<>(Season.Key.class), (seasons) -> {
                seasons.put(Season.Key.SPRING, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.23);
                    phases.put(Season.Phase.MID, 0.26);
                    phases.put(Season.Phase.END, 0.16);
                }));

                seasons.put(Season.Key.SUMMER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.03);
                    phases.put(Season.Phase.MID, 0.0);
                    phases.put(Season.Phase.END, 0.0);
                }));

                seasons.put(Season.Key.AUTUMN, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.03);
                    phases.put(Season.Phase.MID, 0.03);
                    phases.put(Season.Phase.END, 0.03);
                }));

                seasons.put(Season.Key.WINTER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.03);
                    phases.put(Season.Phase.MID, 0.03);
                    phases.put(Season.Phase.END, 0.06);
                }));
            }));

    public static final Blizzard BLIZZARD = new Blizzard(new BlizzardClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0), 0.0F, 0.2F, false, SNOW_LOCATION, SoundRegistry.BLIZZARD_LOOP2, 0.6F, 0.6F), "!#DESERT#SAVANNA", 0.1D, -0.5, 0.1, 2, 10, Blocks.SNOW, true, true, Util.make(new HashMap<>(), ((stringListHashMap) -> stringListHashMap.put(Registry.ENTITY_TYPE.getKey(EntityType.PLAYER).toString(), ImmutableList.of(Registry.EFFECTS.getKey(Effects.SLOWNESS).toString())))), false, 0,
            Util.make(new EnumMap<>(Season.Key.class), (seasons) -> {
                seasons.put(Season.Key.SPRING, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.1);
                    phases.put(Season.Phase.MID, 0.01);
                    phases.put(Season.Phase.END, 0.0);
                }));

                seasons.put(Season.Key.SUMMER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.0);
                    phases.put(Season.Phase.MID, 0.0);
                    phases.put(Season.Phase.END, 0.0);
                }));

                seasons.put(Season.Key.AUTUMN, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.0);
                    phases.put(Season.Phase.MID, 0.01);
                    phases.put(Season.Phase.END, 0.1);
                }));

                seasons.put(Season.Key.WINTER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.3);
                    phases.put(Season.Phase.MID, 0.5);
                    phases.put(Season.Phase.END, 0.45);
                }));
            }));

    public static final Cloudy CLOUDY = new Cloudy(new CloudyClientSettings(RAIN_COLORS, 0.0F, -1.0F, true), "ALL", 0.7D, -0.05, 0.07, false, 0,
            Util.make(new EnumMap<>(Season.Key.class), (map) -> {
                for (Season.Key value : Season.Key.values()) {
                    Map<Season.Phase, Double> phaseDoubleMap = new EnumMap<>(Season.Phase.class);
                    for (Season.Phase phase : Season.Phase.values()) {
                        phaseDoubleMap.put(phase, 0.3D);
                    }
                    map.put(value, phaseDoubleMap);
                }
            }));

    public static final Rain RAIN = new Rain(new RainClientSettings(RAIN_COLORS, 0.0F, -1.0F, true, RAIN_LOCATION, SNOW_LOCATION), "!#DESERT#SAVANNA", 0.7D, -0.5, 0.1, false, 0,
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

    public static final Rain ACID_RAIN_THUNDERING = new AcidRain(new RainClientSettings(THUNDER_COLORS, 0.0F, -1.0F, true, ACID_RAIN_LOCATION, SNOW_LOCATION), "!#DESERT#SAVANNA", 0.125D, -0.1, 0.1, 16, 8, ACID_RAIN_DECAYER, true, 100000,
            Util.make(new EnumMap<>(Season.Key.class), (seasons) -> {
                seasons.put(Season.Key.SPRING, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.115);
                    phases.put(Season.Phase.MID, 0.13);
                    phases.put(Season.Phase.END, 0.08);
                }));

                seasons.put(Season.Key.SUMMER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.0115);
                    phases.put(Season.Phase.MID, 0.0);
                    phases.put(Season.Phase.END, 0.0);
                }));

                seasons.put(Season.Key.AUTUMN, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.0115);
                    phases.put(Season.Phase.MID, 0.0115);
                    phases.put(Season.Phase.END, 0.0115);
                }));

                seasons.put(Season.Key.WINTER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.0115);
                    phases.put(Season.Phase.MID, 0.0115);
                    phases.put(Season.Phase.END, 0.03);
                }));
            }));

    public static final Blizzard BLIZZARD_THUNDERING = new Blizzard(new BlizzardClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0), 0.0F, 0.2F, false, SNOW_LOCATION, SoundRegistry.BLIZZARD_LOOP2, 0.6F, 0.6F), "!#DESERT#SAVANNA", 0.05D, -0.5, 0.1, 2, 10, Blocks.SNOW, true, true, Util.make(new HashMap<>(), ((stringListHashMap) -> stringListHashMap.put(Registry.ENTITY_TYPE.getKey(EntityType.PLAYER).toString(), ImmutableList.of(Registry.EFFECTS.getKey(Effects.SLOWNESS).toString())))), true, 100000,
            Util.make(new EnumMap<>(Season.Key.class), (seasons) -> {
                seasons.put(Season.Key.SPRING, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.05);
                    phases.put(Season.Phase.MID, 0.005);
                    phases.put(Season.Phase.END, 0.0);
                }));

                seasons.put(Season.Key.SUMMER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.0);
                    phases.put(Season.Phase.MID, 0.0);
                    phases.put(Season.Phase.END, 0.0);
                }));

                seasons.put(Season.Key.AUTUMN, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.0);
                    phases.put(Season.Phase.MID, 0.005);
                    phases.put(Season.Phase.END, 0.05);
                }));

                seasons.put(Season.Key.WINTER, Util.make(new EnumMap<>(Season.Phase.class), (phases) -> {
                    phases.put(Season.Phase.START, 0.15);
                    phases.put(Season.Phase.MID, 0.25);
                    phases.put(Season.Phase.END, 0.225);
                }));
            }));

    public static final Cloudy CLOUDY_THUNDERING = new Cloudy(new CloudyClientSettings(THUNDER_COLORS, 0.0F, -0.09F, true), "ALL", 0.1D, -0.05, 0.07, true, 100000,
            Util.make(new EnumMap<>(Season.Key.class), (map) -> {
                for (Season.Key value : Season.Key.values()) {
                    Map<Season.Phase, Double> phaseDoubleMap = new EnumMap<>(Season.Phase.class);
                    for (Season.Phase phase : Season.Phase.values()) {
                        phaseDoubleMap.put(phase, 0.1D);
                    }
                    map.put(value, phaseDoubleMap);
                }
            }));

    public static final Rain THUNDERING = new Rain(new RainClientSettings(THUNDER_COLORS, 0.0F, -1.0F, true, RAIN_LOCATION, SNOW_LOCATION), "!#DESERT#SAVANNA", 0.3D, -0.5, 0.1, true, 100000,
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

    public static final Set<WeatherEvent> DEFAULT_EVENTS = Util.make(new ReferenceArraySet<>(), (set) -> {
        set.add(NONE);
        set.add(ACID_RAIN);
        set.add(BLIZZARD);
        set.add(CLOUDY);
        set.add(RAIN);

        set.add(ACID_RAIN_THUNDERING);
        set.add(BLIZZARD_THUNDERING);
        set.add(CLOUDY_THUNDERING);
        set.add(THUNDERING);
    });

    private WeatherEventClientSettings clientSettings;
    private final String biomeCondition;
    private final double defaultChance;
    private final double temperatureOffsetRaw;
    private final double humidityOffsetRaw;
    private final boolean isThundering;
    private final int lightningFrequency;
    private final Map<Season.Key, Map<Season.Phase, Double>> seasonChances;

    private String name;
    private final ReferenceArraySet<Biome> validBiomes = new ReferenceArraySet<>();

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

    @OnlyIn(Dist.CLIENT)
    public boolean weatherParticlesAndSound(ActiveRenderInfo renderInfo, float ticks, Minecraft mc) {
        this.clientSettings.weatherParticlesAndSound(renderInfo, mc, ticks, this::isValidBiome);
        return true;
    }

    /**
     * This is called in the chunk ticking iterator.
     */
    public void chunkTick(Chunk chunk, ServerWorld world) {
        if (world.rand.nextInt(16) == 0) {
            ChunkPos chunkpos = chunk.getPos();
            int xStart = chunkpos.getXStart();
            int zStart = chunkpos.getZStart();
            BlockPos randomPos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(xStart, 0, zStart, 15));
            BlockPos randomPosDown = randomPos.down();

            Biome biome = world.getBiome(randomPos);
            if (isValidBiome(biome)) {
                if (spawnSnowInFreezingClimates() && biome.doesWaterFreeze(world, randomPosDown)) {
                    world.setBlockState(randomPosDown, Blocks.ICE.getDefaultState());
                }

                if (spawnSnowInFreezingClimates() && biome.doesSnowGenerate(world, randomPos)) {
                    world.setBlockState(randomPos, Blocks.SNOW.getDefaultState());
                }

                if (world.isRainingAt(randomPos.up(25)) && fillBlocksWithWater()) {
                    world.getBlockState(randomPosDown).getBlock().fillWithRain(world, randomPosDown);
                }
            }
        }
    }

    public final void doChunkTick(Chunk chunk, ServerWorld world) {
        chunkTick(chunk, world);
        ChunkPos chunkpos = chunk.getPos();

        if (lightningFrequency < 1) {
            return;
        }
        doLightning(world, chunkpos);
    }

    private void doLightning(ServerWorld world, ChunkPos chunkpos) {
        int xStart = chunkpos.getXStart();
        int zStart = chunkpos.getZStart();
        if (isThundering && world.rand.nextInt(lightningFrequency) == 0) {
            BlockPos blockpos = ((ServerWorldAccess) world).invokeAdjustPosToNearbyEntity(world.getBlockRandomPos(xStart, 0, zStart, 15));
            Biome biome = world.getBiome(blockpos);
            if (isValidBiome(biome)) {
                DifficultyInstance difficultyinstance = world.getDifficultyForLocation(blockpos);
                boolean flag1 = world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && world.rand.nextDouble() < (double) difficultyinstance.getAdditionalDifficulty() * 0.01D;
                if (flag1) {
                    SkeletonHorseEntity skeletonhorseentity = EntityType.SKELETON_HORSE.create(world);
                    skeletonhorseentity.setTrap(true);
                    skeletonhorseentity.setGrowingAge(0);
                    skeletonhorseentity.setPosition((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ());
                    world.addEntity(skeletonhorseentity);
                }

                LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(world);
                lightningboltentity.moveForced(Vector3d.copyCenteredHorizontally(blockpos));
                lightningboltentity.setEffectOnly(flag1);
                world.addEntity(lightningboltentity);
            }
        }
    }

    public boolean fillBlocksWithWater() {
        return false;
    }

    public boolean spawnSnowInFreezingClimates() {
        return false;
    }

    public final TranslationTextComponent successTranslationTextComponent(String key) {
        return new TranslationTextComponent("commands.bw.setweather.success." + key);
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

    @OnlyIn(Dist.CLIENT)
    public boolean renderWeather(Graphics graphics, Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return this.clientSettings.renderWeather(graphics, mc, world, lightTexture, ticks, partialTicks, x, y, z, (this::isValidBiome));
    }

    @OnlyIn(Dist.CLIENT)
    public float skyOpacity(ClientWorld world, BlockPos playerPos) {
        return mixer(world, playerPos, 12, 2.0F, 1.0F - this.clientSettings.skyOpacity());
    }

    @OnlyIn(Dist.CLIENT)
    public float fogDensity(ClientWorld world, BlockPos playerPos) {
        return mixer(world, playerPos, 12, 0.1F, this.clientSettings.fogDensity());
    }


    private float mixer(ClientWorld world, BlockPos playerPos, int transitionRange, float weight, float targetMaxValue) {
        int x = playerPos.getX();
        int z = playerPos.getZ();
        float accumulated = 0.0F;

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int sampleX = x - transitionRange; sampleX <= x + transitionRange; ++sampleX) {
            pos.setX(sampleX);

            for (int sampleZ = z - transitionRange; sampleZ <= z + transitionRange; ++sampleZ) {
                pos.setZ(sampleZ);

                Biome biome = world.getBiome(pos);
                if (validBiomes.contains(biome)) {

                    accumulated += weight * weight;
                }
            }
        }
        float transitionSmoothness = 33 * 33;
        return Math.min(targetMaxValue, (float) Math.sqrt(accumulated / transitionSmoothness));
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3d cloudColor(ClientWorld world, BlockPos playerPos, Vector3d previous) {
        float lerpWeight = mixer(world, playerPos, 12, 0.1F, 1.0F);

        int[] prevColor = ColorUtil.transformFloatColor(previous);
        int mix = ColorUtil.mix(prevColor, ColorUtil.unpack(this.clientSettings.getColorSettings().getTargetSkyHexColor()), Math.min(this.clientSettings.getColorSettings().getSkyColorBlendStrength(), Math.min(world.getRainStrength(Minecraft.getInstance().getRenderPartialTicks()), lerpWeight)));

        float r = (float) (mix >> 16 & 255) / 255.0F;
        float g = (float) (mix >> 8 & 255) / 255.0F;
        float b = (float) (mix & 255) / 255.0F;

        return new Vector3d(r, g, b);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {
        this.clientSettings.clientTick(world, tickSpeed, worldTime, mc, this::isValidBiome);
    }

    public void onWeatherEnd() {
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
}