package corgitaco.betterweather.season;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.BetterWeatherSeasonData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.IWorld;

public class BWSeasons {

    public static void updateSeasonData(IWorld world) {
        BetterWeather.setSeasonData(world);

        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();

        if (currentSeasonTime >= BetterWeather.SEASON_LENGTH) {
            BetterWeatherSeasonData.Season[] seasons = BetterWeatherSeasonData.Season.values();
            int ordinal = BetterWeather.seasonData.getSeason().ordinal();
            BetterWeather.seasonData.setSeason(seasons[seasons.length > ordinal + 1 ? ordinal + 1 : 0].toString());
            BetterWeather.seasonData.setSeasonTime(0);

            if (world instanceof ClientWorld) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.worldRenderer.loadRenderers();
            }

        } else
            BetterWeather.seasonData.setSeasonTime(currentSeasonTime + 1);
    }
}