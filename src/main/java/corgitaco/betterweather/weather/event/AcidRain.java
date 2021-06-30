package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.BetterWeatherUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.IdentityHashMap;
import java.util.Map;

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
        }), Codec.BOOL.fieldOf("isThundering").forGetter(rain -> {
            return rain.isThundering();
        }), Codec.INT.fieldOf("lightningChance").forGetter(rain -> {
            return rain.getLightningChance();
        }), Codec.simpleMap(Season.Key.CODEC, Codec.unboundedMap(Season.Phase.CODEC, Codec.DOUBLE), IStringSerializable.createKeyable(Season.Key.values())).fieldOf("seasonChances").forGetter(rain -> {
            return rain.getSeasonChances();
        })).apply(builder, AcidRain::new);
    });

    protected final int chunkTickChance;
    protected final int entityDamageChance;
    protected final IdentityHashMap<Block, Block> blockToBlock;

    public AcidRain(WeatherEventClientSettings clientSettings, String biomeCondition, double defaultChance, double temperatureOffsetRaw, double humidityOffsetRaw, int chunkTickChance, int entityDamageChance, Map<ResourceLocation, ResourceLocation> blockToBlock, boolean isThundering, int lightningChance, Map<Season.Key, Map<Season.Phase, Double>> seasonChance) {
        super(clientSettings, biomeCondition, defaultChance, temperatureOffsetRaw, humidityOffsetRaw, isThundering, lightningChance, seasonChance);
        this.chunkTickChance = chunkTickChance;
        this.entityDamageChance = entityDamageChance;
        this.blockToBlock = BetterWeatherUtil.transformBlockBlockResourceLocations(blockToBlock);
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

            if (isValidBiome(biome)) {
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
            if (world.getHeight(Heightmap.Type.MOTION_BLOCKING, entityPosition.getX(), entityPosition.getZ()) > entityPosition.getY()) {
                return;
            }

            entity.attackEntityFrom(DamageSource.GENERIC, 0.05F);
        }
    }

    @Override
    public Codec<? extends WeatherEvent> codec() {
        return CODEC;
    }
}
