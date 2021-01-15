package corgitaco.betterweather.weatherevent.weatherevents;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.season.SeasonSystem;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.ServerWorldInfo;

public class WeatherEventUtil {

    public static void doVanillaWeather(IServerWorldInfo worldInfo, ServerWorld world) {
        int clearWeatherTime = worldInfo.getClearWeatherTime();
        int thunderTime = worldInfo.getThunderTime();
        int rainTime = worldInfo.getRainTime();
        boolean isThundering = world.getWorldInfo().isThundering();
        boolean isRaining = world.getWorldInfo().isRaining();
        if (clearWeatherTime > 0) {
            --clearWeatherTime;
            thunderTime = isThundering ? 0 : 1;
            rainTime = isRaining ? 0 : 1;
            isThundering = false;
            isRaining = false;
        } else {
            if (thunderTime > 0) {
                --thunderTime;
                if (thunderTime == 0) {
                    isThundering = !isThundering;
                }
            } else if (isThundering) {
                thunderTime = world.rand.nextInt(12000) + 3600;
            } else {
                thunderTime = world.rand.nextInt(168000) + 12000; //Sets up the time til the very first thunderstorm.
            }

            if (rainTime > 0) {
                --rainTime ;
                if (rainTime == 0) {
                    isRaining = !isRaining ;
                }
            } else if (isRaining) {
                rainTime = world.rand.nextInt(12000) + 12000;
            } else {
                rainTime = world.rand.nextInt(168000) + 12000; //Sets up the time til the very first rain.
            }
        }

        worldInfo.setThunderTime(thunderTime);
        worldInfo.setRainTime(rainTime);
        worldInfo.setClearWeatherTime(clearWeatherTime);
        worldInfo.setThundering(isThundering);
        worldInfo.setRaining(isRaining);
    }

    public static void doWeatherAndRollWeatherEventChance(IServerWorldInfo worldInfo, ServerWorld world) {
        doWeather(worldInfo, world);
        if (BetterWeather.useSeasons)
            SeasonSystem.rollWeatherEventChanceForSeason(world.rand, world, worldInfo.isRaining(), worldInfo.isThundering(), (ServerWorldInfo) worldInfo, world.getPlayers());
        else
            WeatherEventSystem.rollWeatherEventChance(world.rand, world, worldInfo.isRaining(), (ServerWorldInfo) worldInfo, world.getPlayers());

    }

    public static void doWeather(IServerWorldInfo worldInfo, ServerWorld world) {
        int clearWeatherTime = worldInfo.getClearWeatherTime();
        int rainTime = worldInfo.getRainTime();
        boolean isRaining = world.getWorldInfo().isRaining();
        if (clearWeatherTime > 0) {
            --clearWeatherTime;
            rainTime = isRaining ? 0 : 1;
            isRaining = false;
        } else {
            if (rainTime > 0) {
                --rainTime ;
                if (rainTime == 0) {
                    isRaining = !isRaining ;
                }
            } else if (isRaining) {
                rainTime = world.rand.nextInt(12000) + 12000;
            } else {
                rainTime = world.rand.nextInt(168000) + 12000; //Sets up the time til the very first precipitation upon world creation.
            }
        }
        worldInfo.setRainTime(rainTime);
        worldInfo.setClearWeatherTime(clearWeatherTime);
        worldInfo.setRaining(isRaining);
    }

    public static void vanillaIceAndSnowChunkTicks(Chunk chunk, ServerWorld world) {
        ChunkPos chunkpos = chunk.getPos();
        int xStart = chunkpos.getXStart();
        int zStart = chunkpos.getZStart();
        boolean isRaining = world.isRaining();
        BlockPos randomPos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(xStart, 0, zStart, 15));
        BlockPos randomPosDown = randomPos.down();

        Biome biome = world.getBiome(randomPos);


        if (world.isAreaLoaded(randomPos, 1)) { // Forge: check area to avoid loading neighbors in unloaded chunks
            if (biome.doesWaterFreeze(world, randomPosDown)) {
                world.setBlockState(randomPosDown, Blocks.ICE.getDefaultState());
            }
        }

        if (isRaining && biome.doesSnowGenerate(world, randomPos) && WeatherData.currentWeatherEvent.spawnSnowInFreezingClimates()) {
            world.setBlockState(randomPos, Blocks.SNOW.getDefaultState());
        }

        if (isRaining && biome.getPrecipitation() == Biome.RainType.RAIN && WeatherData.currentWeatherEvent.fillBlocksWithWater()) {
            world.getBlockState(randomPosDown).getBlock().fillWithRain(world, randomPosDown);
        }
    }
}
