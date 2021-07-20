package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.BWSeason;
import corgitaco.betterweather.season.SeasonContext;
import corgitaco.betterweather.season.SubseasonSnowSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import corgitaco.betterweather.weather.event.client.settings.NoneClientSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class None extends WeatherEvent {

    public static final Codec<None> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(WeatherEventClientSettings.CODEC.fieldOf("clientSettings").forGetter((none) -> {
            return none.getClientSettings();
        })).apply(builder, None::new);
    });

    public static final None DEFAULT = new None(new NoneClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0)));


    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(WeatherEvent.VALUE_COMMENTS), (map) -> {
    }), true);


    public None(WeatherEventClientSettings clientSettings) {
        super(clientSettings, "ALL", 0.0, 0.0, 0.0, false, 0, NO_SEASON_CHANCES);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {

    }

    @Override
    public void onChunkLoad(Chunk chunk, ServerWorld world) {
        SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
        if (seasonContext != null) {
            BWSeason currentSeason = seasonContext.getCurrentSeason();
            SubseasonSnowSettings snowSettings = currentSeason.getCurrentSettings().getSnowSettings();
            boolean spawnSnow = snowSettings.getSnowType() == SubseasonSnowSettings.SnowType.SPAWN;
            ChunkPos pos = chunk.getPos();

            int chunkMinX = pos.getXStart();
            int chunkMinZ = pos.getZStart();
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            List<BlockPos> currentSnowPositions = new ArrayList<>();
            List<BlockPos> totalSnowPositions = new ArrayList<>();
            List<BlockPos> currentlyAvailableSnowPositions = new ArrayList<>();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING).getHeight(x, z);

                    BlockState blockState = chunk.getBlockState(mutable.setPos(chunkMinX + x, height, chunkMinZ + z));
                    Biome biome = world.getBiome(mutable);

                    boolean hasSnow = blockState.getBlock() == Blocks.SNOW;
                    if (hasSnow) {
                        currentSnowPositions.add(mutable.toImmutable());
                    }

                    if (biome.getTemperature(mutable) >= 0.15 && Blocks.SNOW.getDefaultState().isValidPosition(world, mutable)) {
                        if (!hasSnow) {
                            currentlyAvailableSnowPositions.add(mutable.toImmutable());
                        }
                        totalSnowPositions.add(mutable.toImmutable());
                    }

                }
            }

            if (spawnSnow) {
                if (currentSnowPositions.size() > 0 && currentlyAvailableSnowPositions.size() > 0) {
                    // Current percentage of not covered snow positions.
                    double notCoveredPercent = (double) currentlyAvailableSnowPositions.size() / totalSnowPositions.size();


                    // Required snow coverage when loading this chunk
                    double minimum = snowSettings.getMinimum();


                    if (notCoveredPercent < minimum) {

                        // Total number of snow blocks the
                        int snowBlockCount = (int) Math.round(((notCoveredPercent) * (totalSnowPositions.size() + 1)));

                        for (int addedSnowCount = 0; addedSnowCount < snowBlockCount; snowBlockCount++) {
                            int index = world.rand.nextInt(currentlyAvailableSnowPositions.size());
                            BlockPos blockPos = currentlyAvailableSnowPositions.get(index);

                            world.setBlockState(blockPos, Blocks.SNOW.getDefaultState(), 2);
                            currentlyAvailableSnowPositions.remove(index);
                        }
                    }
                }
            } else {
//                double amountCovered = (double) totalSnowPositions.size() / currentSnowPositions.size();
//
//                double minimum = snowSettings.getMinimum();
//                if (amountCovered > minimum) {
//                    int requiredMinimumCount = (int) (minimum * totalSnowPositions.size());
//                    int count = currentlyAvailableSnowPositions.size() - requiredMinimumCount;
//
//                    for (int addedSnowCount = 0; addedSnowCount < count; count++) {
//                        int index = world.rand.nextInt(currentlyAvailableSnowPositions.size() - 1);
//                        BlockPos blockPos = currentlyAvailableSnowPositions.get(index);
//
//                        world.setBlockState(blockPos, Blocks.SNOW.getDefaultState(), 2);
//                        currentlyAvailableSnowPositions.remove(index);
//                    }
//                }
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

    @Override
    public double getTemperatureModifierAtPosition(BlockPos pos) {
        return getTemperatureOffsetRaw();
    }

    @Override
    public double getHumidityModifierAtPosition(BlockPos pos) {
        return getHumidityOffsetRaw();
    }
}
