package corgitaco.betterweather.common.snow;

import corgitaco.betterweather.common.savedata.BetterWeatherChunkData;
import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.common.weather.WeatherContext;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

/**
 * Snow logic for covering unloaded chunks upon chunk load.
 */
public class SnowController {

    private final ServerWorld world;
    @Nullable
    private final WeatherContext weatherContext;
    @Nullable
    private final SeasonContext seasonContext;

    public SnowController(ServerWorld world, @Nullable WeatherContext weatherContext, @Nullable SeasonContext seasonContext){
        this.world = world;
        this.weatherContext = weatherContext;
        this.seasonContext = seasonContext;
    }

    public void onChunkLoad(Chunk chunk) {
        if (weatherContext != null && seasonContext != null) {
            BetterWeatherChunkData betterWeatherChunkData = ((BetterWeatherChunkData.Access) chunk).get();
        }
    }

    public interface Access {

        SnowController get();
    }
}