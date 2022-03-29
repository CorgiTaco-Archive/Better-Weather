package corgitaco.betterweather.common.snow;

import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.common.savedata.BetterWeatherChunkData;
import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.common.weather.WeatherEventInstance;
import corgitaco.betterweather.common.weather.WeatherForecast;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Snow logic for covering unloaded chunks upon chunk load.
 */
public class SnowIceController {


    private final ServerWorld world;
    @Nullable
    private final WeatherContext weatherContext;
    @Nullable
    private final SeasonContext seasonContext;

    public SnowIceController(ServerWorld world, @Nullable WeatherContext weatherContext, @Nullable SeasonContext seasonContext) {
        this.world = world;
        this.weatherContext = weatherContext;
        this.seasonContext = seasonContext;
    }

    public void onChunkLoad(Chunk chunk) {
        if (weatherContext != null && seasonContext != null) {
            double coveragePercent = 1.0;
            Season.Key currentSeason = seasonContext.getCurrentSeason().getSeasonKey();
            Season.Phase currentPhase = seasonContext.getCurrentSeason().getCurrentPhase();

            ArrayList<WeatherEventInstance> relevantWeatherEvents = new ArrayList<>();
            WeatherForecast weatherForecast = this.weatherContext.getWeatherForecast();
            WeatherEventInstance weatherEventInstance = weatherForecast.getForecast().get(0);
            long dayTime = world.getDayTime();
            if (weatherEventInstance.active(dayTime, weatherContext.getDayLength())) {
                relevantWeatherEvents.add(weatherEventInstance);
            }
            for (WeatherEventInstance pastEvent : weatherForecast.getPastEvents()) {
                if (pastEvent.getTimeSinceEvent(dayTime, weatherContext.getDayLength()) <= 240000) {
                    relevantWeatherEvents.add(pastEvent);
                }
            }
            if (!relevantWeatherEvents.isEmpty()) {

                for (WeatherEventInstance relevantWeatherEvent : relevantWeatherEvents) {
                    long timeSinceEvent = Math.max(0L, relevantWeatherEvent.getTimeSinceEvent(dayTime, weatherContext.getDayLength()));
                    int yearLength = seasonContext.getYearLength();
                    long yearTimeForEvent = dayTime - timeSinceEvent % yearLength;
                    Season.Key seasonForEvent = Season.getSeasonFromTime(yearTimeForEvent, yearLength);
                }
                BetterWeatherChunkData betterWeatherChunkData = ((BetterWeatherChunkData.Access) chunk).get();

                List<BlockPos> positions = new ArrayList<>();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int height = chunk.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z) + 1;
                        positions.add(new BlockPos(SectionPos.sectionToBlockCoord(chunk.getPos().x) + x, height, SectionPos.sectionToBlockCoord(chunk.getPos().z) + z));
                    }
                }
                Collections.shuffle(positions, new Random(world.getSeed() + relevantWeatherEvents.get(0).getEventLengthInTicks() + chunk.getPos().toLong()));

                for (int i = 0; i < positions.size() * MathHelper.clamp(coveragePercent, 0, 1); i++) {
                    BlockPos blockPos = positions.get(i);

                    if (world.getBiome(blockPos).shouldSnow(world, blockPos)) {
                        chunk.setBlockState(blockPos, Blocks.SNOW.defaultBlockState(), false);
                    }
                }
            }
        }
    }

    public interface Access {

        SnowIceController get();
    }
}