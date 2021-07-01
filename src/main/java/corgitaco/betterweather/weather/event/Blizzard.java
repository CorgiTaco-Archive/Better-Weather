package corgitaco.betterweather.weather.event;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.core.SoundRegistry;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import corgitaco.betterweather.weather.event.client.BlizzardClientSettings;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("deprecation")
public class Blizzard extends WeatherEvent {

    public static final Codec<Blizzard> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(WeatherEventClientSettings.CODEC.fieldOf("clientSettings").forGetter((blizzard) -> {
            return blizzard.getClientSettings();
        }), Codec.STRING.fieldOf("biomeCondition").forGetter(blizzard -> {
            return blizzard.getBiomeCondition();
        }), Codec.DOUBLE.fieldOf("temperatureOffset").forGetter(blizzard -> {
            return blizzard.getTemperatureOffsetRaw();
        }), Codec.DOUBLE.fieldOf("humidityOffset").forGetter(blizzard -> {
            return blizzard.getHumidityOffsetRaw();
        }), Codec.DOUBLE.fieldOf("defaultChance").forGetter(blizzard -> {
            return blizzard.getDefaultChance();
        }), Codec.INT.fieldOf("blockLightThreshold").forGetter(blizzard -> {
            return blizzard.blockLightThreshold;
        }), Codec.INT.fieldOf("tickChance").forGetter(blizzard -> {
            return blizzard.tickChance;
        }), ResourceLocation.CODEC.fieldOf("snowBlock").forGetter(blizzard -> {
            return Registry.BLOCK.getKey(blizzard.snowBlock);
        }), Codec.BOOL.fieldOf("snowLayering").forGetter(blizzard -> {
            return blizzard.snowLayers;
        }), Codec.BOOL.fieldOf("waterFreezes").forGetter(blizzard -> {
            return blizzard.waterFreezes;
        }), Codec.unboundedMap(Codec.STRING, Codec.list(Codec.STRING)).fieldOf("entityEffectsMap").forGetter(blizzard -> {
            return blizzard.entityOrCategoryToEffectsMap;
        }), Codec.BOOL.fieldOf("isThundering").forGetter(rain -> {
            return rain.isThundering();
        }), Codec.INT.fieldOf("lightningChance").forGetter(rain -> {
            return rain.getLightningChance();
        }), Codec.simpleMap(Season.Key.CODEC, Codec.unboundedMap(Season.Phase.CODEC, Codec.DOUBLE), IStringSerializable.createKeyable(Season.Key.values())).fieldOf("seasonChances").forGetter(blizzard -> {
            return blizzard.getSeasonChances();
        })).apply(builder, (clientSettings, biomeCondition, temperatureOffsetRaw, humidityOffsetRaw, defaultChance, tickRate, blockLightThreshold, snowBlockID, snowLayers, waterFreezes, entityOrCategoryToEffectsMap, isThundering, lightningChance, map) -> {
            Optional<Block> blockOptional = Registry.BLOCK.getOptional(snowBlockID);
            if (!blockOptional.isPresent()) {
                BetterWeather.LOGGER.error("\"" + snowBlockID.toString() + "\" is not a valid block ID in the registry, defaulting to \"minecraft:snow\"...");
            }


            return new Blizzard(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, tickRate, blockLightThreshold, blockOptional.orElse(Blocks.SNOW), snowLayers, waterFreezes, entityOrCategoryToEffectsMap, isThundering, lightningChance, map);
        });
    });

    public static final Blizzard DEFAULT = new Blizzard(new BlizzardClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0), 0.0F, 0.2F, false, Rain.SNOW_LOCATION, SoundRegistry.BLIZZARD_LOOP2, 0.6F, 0.6F), "!#DESERT#SAVANNA", 0.1D, -0.5, 0.1, 2, 10, Blocks.SNOW, true, true, Util.make(new HashMap<>(), ((stringListHashMap) -> stringListHashMap.put(Registry.ENTITY_TYPE.getKey(EntityType.PLAYER).toString(), ImmutableList.of(Registry.EFFECTS.getKey(Effects.SLOWNESS).toString())))), false, 0,
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

    public static final Blizzard DEFAULT_THUNDERING = new Blizzard(new BlizzardClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0), 0.0F, 0.2F, false, Rain.SNOW_LOCATION, SoundRegistry.BLIZZARD_LOOP2, 0.6F, 0.6F), "!#DESERT#SAVANNA", 0.05D, -0.5, 0.1, 2, 10, Blocks.SNOW, true, true, Util.make(new HashMap<>(), ((stringListHashMap) -> stringListHashMap.put(Registry.ENTITY_TYPE.getKey(EntityType.PLAYER).toString(), ImmutableList.of(Registry.EFFECTS.getKey(Effects.SLOWNESS).toString())))), true, 100000,
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


    public static final Map<EntityClassification, List<EntityType<?>>> CLASSIFICATION_ENTITY_TYPES = Util.make(new EnumMap<>(EntityClassification.class), (map) -> {
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            map.computeIfAbsent(entityType.getClassification(), (mobCategory -> new ArrayList<>())).add(entityType);
        }
    });

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(WeatherEvent.VALUE_COMMENTS), (map) -> {
    }), true);
    private final int tickChance;
    private final int blockLightThreshold;
    private final Block snowBlock;
    private final boolean snowLayers;
    private final boolean waterFreezes;
    private final Map<String, List<String>> entityOrCategoryToEffectsMap;
    private final Map<EntityType<?>, List<EffectInstance>> entityTypeToEffectMap = new Reference2ReferenceArrayMap<>();


    public Blizzard(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, int tickChance, int blockLightThreshold, Block snowBlock, boolean snowLayers, boolean waterFreezes, Map<String, List<String>> entityOrCategoryToEffectsMap, boolean isThundering, int lightningChance, Map<Season.Key, Map<Season.Phase, Double>> map) {
        super(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, isThundering, lightningChance, map);
        this.tickChance = tickChance;
        this.blockLightThreshold = blockLightThreshold;
        this.snowBlock = snowBlock;
        this.snowLayers = snowLayers;
        this.waterFreezes = waterFreezes;
        this.entityOrCategoryToEffectsMap = entityOrCategoryToEffectsMap;

        for (Map.Entry<String, List<String>> entry : entityOrCategoryToEffectsMap.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            if (key.startsWith("category/")) {
                String mobCategory = key.substring("category/".length()).toUpperCase();

                EntityClassification[] values = EntityClassification.values();
                if (Arrays.stream(values).noneMatch(difficulty -> difficulty.toString().equals(mobCategory))) {
                    BetterWeather.LOGGER.error("\"" + mobCategory + "\" is not a valid mob category value. Skipping mob category entry...\nValid Mob Categories: " + Arrays.toString(values));
                    continue;
                }

                for (EntityType<?> entityType : CLASSIFICATION_ENTITY_TYPES.get(EntityClassification.valueOf(mobCategory))) {
                    addEntry(value, entityType);
                }
                continue;
            }

            ResourceLocation entityTypeID = tryParse(key.toLowerCase());
            if (entityTypeID != null && !Registry.ENTITY_TYPE.keySet().contains(entityTypeID)) {
                BetterWeather.LOGGER.error("\"" + key + "\" is not a valid entity ID. Skipping entry...");
                continue;
            }
            addEntry(value, Registry.ENTITY_TYPE.getOptional(entityTypeID).get());
        }
    }

    private void addEntry(List<String> value, EntityType<?> entityType) {
        List<EffectInstance> effects = new ArrayList<>();
        for (String effectArguments : value) {
            EffectInstance effectInstanceFromString = createEffectInstanceFromString(effectArguments);
            if (effectInstanceFromString != null) {
                effects.add(effectInstanceFromString);
            }
        }


        this.entityTypeToEffectMap.put(entityType, effects);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {

    }

    @Override
    public void chunkTick(Chunk chunk, ServerWorld world) {
        if (this.tickChance < 1) {
            return;
        }
        if (world.rand.nextInt(tickChance) == 0) {
            ChunkPos chunkpos = chunk.getPos();
            int xStart = chunkpos.getXStart();
            int zStart = chunkpos.getZStart();
            BlockPos randomHeightMapPos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(xStart, 0, zStart, 15));
            BlockPos randomPosDown = randomHeightMapPos.down();
            BlockState blockState = world.getBlockState(randomHeightMapPos);

            Biome biome = world.getBiome(randomHeightMapPos);
            if (!isValidBiome(biome)) {
                return;
            }

            if (waterFreezes) {
                if (biome.doesWaterFreeze(world, randomPosDown)) {
                    world.setBlockState(randomPosDown, Blocks.ICE.getDefaultState());
                }
            }

            if (meetsStateRequirements(world, randomHeightMapPos)) {
                world.setBlockState(randomHeightMapPos, this.snowBlock.getDefaultState());
                return;
            }

            if (this.snowLayers) {
                if (meetsLayeringRequirement(world, randomHeightMapPos)) {
                    int currentLayer = blockState.get(BlockStateProperties.LAYERS_1_8);

                    if (currentLayer < 7) {
                        world.setBlockState(randomHeightMapPos, blockState.with(BlockStateProperties.LAYERS_1_8, currentLayer + 1), 2);
                    }
                }
            }
        }
    }

    private boolean meetsStateRequirements(IWorldReader worldIn, BlockPos pos) {
        if (pos.getY() >= 0 && pos.getY() < worldIn.getHeight() && worldIn.getLightFor(LightType.BLOCK, pos) < this.blockLightThreshold) {
            BlockState blockstate = worldIn.getBlockState(pos);
            BlockState defaultState = this.snowBlock.getDefaultState();
            return (blockstate.isAir(worldIn, pos) && defaultState.isValidPosition(worldIn, pos));
        }

        return false;
    }

    private boolean meetsLayeringRequirement(IWorldReader worldIn, BlockPos pos) {
        BlockState blockstate = worldIn.getBlockState(pos);
        BlockState defaultState = this.snowBlock.getDefaultState();
        return (defaultState.hasProperty(BlockStateProperties.LAYERS_1_8) && this.snowLayers && blockstate.getBlock() == this.snowBlock);
    }


    @Override
    public boolean spawnSnowInFreezingClimates() {
        return true;
    }

    @Override
    public void livingEntityUpdate(LivingEntity entity) {
//        World world = entity.world;
//        if (!isValidBiome(world.getBiome(entity.getPosition()))) {
//            return;
//        }
//
//        if(world.getWorldInfo().getGameTime() % 20 == 0) {
//            if (entityTypeToEffectMap.containsKey(entity.getType())) {
//                for (EffectInstance effectInstance : entityTypeToEffectMap.get(entity.getType())) {
//                    if (!entity.isPotionActive(effectInstance.getPotion())) {
//                        entity.addPotionEffect(effectInstance);
//                    }
//                }
//            }
//        }
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

    @Nullable
    private static EffectInstance createEffectInstanceFromString(String effectString) {
        String[] split = effectString.split("(?=[\\$])");

        Effect effect = null;
        int amplifier = 4;

        for (int i = 0; i < split.length; i++) {
            String variable = split[i];
            if (i == 0) {
                if (!(variable.startsWith("$") || variable.startsWith("#"))) {
                    ResourceLocation resourceLocation = tryParse(variable);
                    if (resourceLocation != null) {
                        if (Registry.EFFECTS.keySet().contains(resourceLocation)) {
                            effect = Registry.EFFECTS.getOptional(resourceLocation).get();
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    if (variable.startsWith("$")) {
                        String substring = variable.substring(1);
                        try {
                            if (!substring.isEmpty()) {
                                amplifier = Integer.getInteger(substring);
                            }
                        } catch (NumberFormatException e) {
                            BetterWeather.LOGGER.error("Not a number: " + substring);
                        }
                    }
                }
            }
        }
        return effect != null ? new EffectInstance(effect, 5, amplifier, true, false, false) : null;
    }

    @Nullable
    public static ResourceLocation tryParse(String id) {
        try {
            return new ResourceLocation(id);
        } catch (ResourceLocationException resourcelocationexception) {
            BetterWeather.LOGGER.error(resourcelocationexception.getMessage());
            return null;
        }
    }

}
