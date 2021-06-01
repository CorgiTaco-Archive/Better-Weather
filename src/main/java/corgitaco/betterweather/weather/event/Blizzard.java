package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        }), Codec.INT.fieldOf("tickRate").forGetter(blizzard -> {
            return blizzard.tickRate;
        }), ResourceLocation.CODEC.fieldOf("snowBlock").forGetter(blizzard -> {
            return Registry.BLOCK.getKey(blizzard.snowBlock);
        }), Codec.BOOL.fieldOf("snowLayering").forGetter(blizzard -> {
            return blizzard.snowLayers;
        }), Codec.BOOL.fieldOf("waterFreezes").forGetter(blizzard -> {
            return blizzard.waterFreezes;
        }), Codec.simpleMap(Season.Key.CODEC, Codec.unboundedMap(Season.Phase.CODEC, Codec.DOUBLE), IStringSerializable.createKeyable(Season.Key.values())).fieldOf("seasonChances").forGetter(blizzard -> {
            return blizzard.getSeasonChances();
        })).apply(builder, (clientSettings, biomeCondition, temperatureOffsetRaw, humidityOffsetRaw, defaultChance, tickRate, blockLightThreshold, snowBlockID, snowLayers, waterFreezes, map) -> {
            Optional<Block> blockOptional = Registry.BLOCK.getOptional(snowBlockID);
            if (!blockOptional.isPresent()) {
                BetterWeather.LOGGER.error("\"" + snowBlockID.toString() + "\" is not a valid block ID in the registry, defaulting to \"minecraft:snow\"...");
            }


            return new Blizzard(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, tickRate, blockLightThreshold, blockOptional.orElse(Blocks.SNOW), snowLayers, waterFreezes, map);
        });
    });

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(WeatherEvent.VALUE_COMMENTS), (map) -> {
    }), true);
    private final int tickRate;
    private final int blockLightThreshold;
    private final Block snowBlock;
    private final boolean snowLayers;
    private final boolean waterFreezes;


    public Blizzard(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, int tickRate, int blockLightThreshold, Block snowBlock, boolean snowLayers, boolean waterFreezes, Map<Season.Key, Map<Season.Phase, Double>> map) {
        super(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, map);
        this.tickRate = tickRate;
        this.blockLightThreshold = blockLightThreshold;
        this.snowBlock = snowBlock;
        this.snowLayers = snowLayers;
        this.waterFreezes = waterFreezes;
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {

    }

    @Override
    public void tickLiveChunks(Chunk chunk, ServerWorld world) {
        if (world.rand.nextInt(Math.max(Math.abs(this.tickRate), 1)) == 0) {
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
