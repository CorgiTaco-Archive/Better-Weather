package corgitaco.betterweather.season;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.BetterWeatherSeasonData;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.SeasonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BWSeasons {

    public static void updateSeasonPacket(PlayerEntity player, World world) {
        BetterWeather.setSeasonData(world);
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();

        if (currentSeasonTime >= BetterWeather.SEASON_LENGTH) {
            BetterWeatherSeasonData.Season[] seasons = BetterWeatherSeasonData.Season.values();
            int ordinal = BetterWeather.seasonData.getSeason().ordinal();
            BetterWeather.seasonData.setSeason(seasons[seasons.length > ordinal + 1 ? ordinal + 1 : 0].toString());
            BetterWeather.seasonData.setSeasonTime(0);
        } else {
            BetterWeather.seasonData.setSeasonTime(currentSeasonTime + 1);
        }

        NetworkHandler.sendTo((ServerPlayerEntity) player, new SeasonPacket(BetterWeather.seasonData.getSeasonTime()));
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSeason() {
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();
        if (currentSeasonTime >= BetterWeather.SEASON_LENGTH) {
            BetterWeatherSeasonData.Season[] seasons = BetterWeatherSeasonData.Season.values();
            int ordinal = BetterWeather.seasonData.getSeason().ordinal();
            BetterWeather.seasonData.setSeason(seasons[seasons.length > ordinal + 1 ? ordinal + 1 : 0].toString());
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.worldRenderer.loadRenderers();
        }
    }
}