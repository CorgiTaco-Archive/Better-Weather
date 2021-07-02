package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.BetterWeatherUtil;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import corgitaco.betterweather.weather.event.client.settings.RainClientSettings;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

public class AcidRain extends Rain {

    public static final Codec<AcidRain> CODEC = RecordCodecBuilder.create((builder) -> {
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
        }), Codec.INT.fieldOf("chunkTickChance").forGetter(blizzard -> {
            return blizzard.chunkTickChance;
        }), Codec.INT.fieldOf("entityDamageChance").forGetter(blizzard -> {
            return blizzard.entityDamageChance;
        }), Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).fieldOf("decayer").forGetter(rain -> {
            return BetterWeatherUtil.transformBlockBlocksToResourceLocations(rain.blockToBlock);
        }), Codec.unboundedMap(Codec.STRING, Codec.FLOAT).fieldOf("entityDamage").forGetter(rain -> {
            return rain.entityDamageSerializable;
        }), Codec.BOOL.fieldOf("isThundering").forGetter(rain -> {
            return rain.isThundering();
        }), Codec.INT.fieldOf("lightningChance").forGetter(rain -> {
            return rain.getLightningChance();
        }), Codec.simpleMap(Season.Key.CODEC, Codec.unboundedMap(Season.Phase.CODEC, Codec.DOUBLE), IStringSerializable.createKeyable(Season.Key.values())).fieldOf("seasonChances").forGetter(rain -> {
            return rain.getSeasonChances();
        })).apply(builder, AcidRain::new);
    });

    public static final Map<String, String> VALUE_COMMENTS = Util.make(new HashMap<>(WeatherEvent.VALUE_COMMENTS), (map) -> {
        map.putAll(RainClientSettings.VALUE_COMMENTS);
        map.put("entityDamageChance", "The chance of an entity getting damaged every tick when acid rain is on the player's position.");
        map.put("decayer", "What the specified block(left) \"decays\" into(right).");
        map.put("entityDamage", "Entity/Category(left) damage strength(right).");
        map.put("chunkTickChance", "The chance of a chunk being ticked for this tick.");
    });

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(VALUE_COMMENTS, true);

    public static final ResourceLocation ACID_RAIN_LOCATION = new ResourceLocation(BetterWeather.MOD_ID, "textures/environment/acid_rain.png");

    public static final IdentityHashMap<ResourceLocation, ResourceLocation> DEFAULT_DECAYER = Util.make(new IdentityHashMap<>(), (map) -> {
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

    public static final HashMap<String, Float> DEFAULT_ENTITY_DAMAGE = Util.make(new HashMap<>(), (map) -> {
        map.put("category/monster", 0.5F);
        map.put("minecraft:player", 0.5F);
    });

    public static final AcidRain DEFAULT = new AcidRain(new RainClientSettings(RAIN_COLORS, 0.0F, -1.0F, true, ACID_RAIN_LOCATION, SNOW_LOCATION), DEFAULT_BIOME_CONDITION, 0.25D, -0.1, 0.1, 150, 100, AcidRain.DEFAULT_DECAYER, AcidRain.DEFAULT_ENTITY_DAMAGE, false, 0,
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

    public static final AcidRain DEFAULT_THUNDERING = new AcidRain(new RainClientSettings(THUNDER_COLORS, 0.0F, -1.0F, true, ACID_RAIN_LOCATION, SNOW_LOCATION), DEFAULT_BIOME_CONDITION, 0.125D, -0.1, 0.1, 150, 100, DEFAULT_DECAYER, DEFAULT_ENTITY_DAMAGE, true, 100000,
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

    private final int chunkTickChance;
    private final int entityDamageChance;
    private final IdentityHashMap<Block, Block> blockToBlock;
    private final Map<String, Float> entityDamageSerializable;
    private final Object2FloatArrayMap<EntityType<?>> entityDamage = new Object2FloatArrayMap<>();


    public AcidRain(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, int chunkTickChance, int entityDamageChance, Map<ResourceLocation, ResourceLocation> blockToBlock, Map<String, Float> entityDamage, boolean isThundering, int lightningChance, Map<Season.Key, Map<Season.Phase, Double>> seasonChance) {
        super(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, isThundering, lightningChance, seasonChance);
        this.chunkTickChance = chunkTickChance;
        this.entityDamageChance = entityDamageChance;
        this.blockToBlock = BetterWeatherUtil.transformBlockBlockResourceLocations(blockToBlock);
        this.entityDamageSerializable = entityDamage;

        for (Map.Entry<String, Float> entry : entityDamage.entrySet()) {
            String key = entry.getKey();
            float value = entry.getValue();
            if (key.startsWith("category/")) {
                String mobCategory = key.substring("category/".length()).toUpperCase();

                EntityClassification[] values = EntityClassification.values();
                if (Arrays.stream(values).noneMatch(difficulty -> difficulty.toString().equals(mobCategory))) {
                    BetterWeather.LOGGER.error("\"" + mobCategory + "\" is not a valid mob category value. Skipping mob category entry...\nValid Mob Categories: " + Arrays.toString(values));
                    continue;
                }

                for (EntityType<?> entityType : Blizzard.CLASSIFICATION_ENTITY_TYPES.get(EntityClassification.valueOf(mobCategory))) {
                    this.entityDamage.put(entityType, value);
                }
                continue;
            }

            ResourceLocation entityTypeID = Blizzard.tryParse(key.toLowerCase());
            if (entityTypeID != null && !Registry.ENTITY_TYPE.keySet().contains(entityTypeID)) {
                BetterWeather.LOGGER.error("\"" + key + "\" is not a valid entity ID. Skipping entry...");
                continue;
            }
            this.entityDamage.put(Registry.ENTITY_TYPE.getOptional(entityTypeID).get(), value);
        }
    }

    @Override
    public void chunkTick(Chunk chunk, ServerWorld world) {
        super.chunkTick(chunk, world);
        if (this.chunkTickChance < 1) {
            return;
        }
        if (world.rand.nextInt(chunkTickChance) == 0) {
            ChunkPos chunkpos = chunk.getPos();
            int xStart = chunkpos.getXStart();
            int zStart = chunkpos.getZStart();
            BlockPos randomPos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(xStart, 0, zStart, 15));
            BlockPos randomPosDown = randomPos.down();
            Biome biome = world.getBiome(randomPos);

            if (isValidBiome(biome) && !biome.doesSnowGenerate(world, randomPos)) {
                Block currentBlock = world.getBlockState(randomPos).getBlock();
                Block currentBlockDown = world.getBlockState(randomPosDown).getBlock();

                if (this.blockToBlock.containsKey(currentBlock)) {
                    world.setBlockState(randomPos, this.blockToBlock.get(currentBlock).getDefaultState());
                }
                if (this.blockToBlock.containsKey(currentBlockDown)) {
                    world.setBlockState(randomPosDown, this.blockToBlock.get(currentBlockDown).getDefaultState());
                }
            }
        }
    }


    @Override
    public void livingEntityUpdate(LivingEntity entity) {
        if (this.chunkTickChance < 1) {
            return;
        }
        World world = entity.world;
        if (world.rand.nextInt(entityDamageChance) == 0) {
            BlockPos entityPosition = entity.getPosition();
            Biome biome = world.getBiome(entityPosition);
            if (world.getHeight(Heightmap.Type.MOTION_BLOCKING, entityPosition.getX(), entityPosition.getZ()) > entityPosition.getY() || !isValidBiome(biome) || biome.doesSnowGenerate(world, entityPosition)) {
                return;
            }

            if (this.entityDamage.containsKey(entity.getType())) {
                entity.attackEntityFrom(DamageSource.GENERIC, this.entityDamage.getFloat(entity.getType()));
            }
        }
    }

    @Override
    public Codec<? extends WeatherEvent> codec() {
        return CODEC;
    }

    @Override
    public DynamicOps<?> configOps() {
        return CONFIG_OPS;
    }
}
