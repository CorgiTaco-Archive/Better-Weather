package corgitaco.betterweather.weatherevent.weatherevents;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.SeasonSystem;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
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

    public static void doVanillaWeatherAndRollWeatherEventChance(IServerWorldInfo worldInfo, ServerWorld world) {
        doWeather(worldInfo, world);
        if (BetterWeather.useSeasons)
            SeasonSystem.rollWeatherEventChanceForSeason(world.rand, worldInfo.isRaining(), worldInfo.isThundering(), (ServerWorldInfo) worldInfo, world.getPlayers());
        else
            WeatherEventSystem.rollWeatherEventChance(world.rand, worldInfo.isRaining(), (ServerWorldInfo) worldInfo, world.getPlayers());

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
                rainTime = world.rand.nextInt(168000) + 12000; //Sets up the time til the very first rain.
            }
        }
        worldInfo.setRainTime(rainTime);
        worldInfo.setClearWeatherTime(clearWeatherTime);
        worldInfo.setRaining(isRaining);
    }
}
