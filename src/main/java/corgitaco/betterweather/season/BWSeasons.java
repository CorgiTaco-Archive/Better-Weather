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

    public static BetterWeatherSeasonData.SubSeason cachedSubSeason = BetterWeatherSeasonData.SubSeason.SPRING_START;
    public static BetterWeatherSeasonData.Season cachedSeason = BetterWeatherSeasonData.Season.SPRING;

    public static void seasonTime() {
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();
        if (currentSeasonTime > BetterWeather.SEASON_CYCLE_LENGTH)
            BetterWeather.seasonData.setSeasonTime(0);
        else
            BetterWeather.seasonData.setSeasonTime(currentSeasonTime + 1);

    }

    public static void updateSeasonPacket(PlayerEntity player, World world) {
        BetterWeather.setSeasonData(world);
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();

        BetterWeatherSeasonData.SubSeason subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength());

        if (cachedSubSeason != subSeason) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
//            BetterWeather.LOGGER.info(subSeason.toString());
        }


        NetworkHandler.sendTo((ServerPlayerEntity) player, new SeasonPacket(BetterWeather.seasonData.getSeasonTime(), BetterWeather.SEASON_CYCLE_LENGTH));
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSeason() {
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();

        BetterWeatherSeasonData.SubSeason subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength());

        if (cachedSubSeason != subSeason) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
            Minecraft minecraft = Minecraft.getInstance();
            cachedSubSeason = subSeason;
            minecraft.worldRenderer.loadRenderers();
        }
    }

    public static BetterWeatherSeasonData.SubSeason getSubSeasonFromTime(int seasonTime, int seasonCycleLength) {
        int perSeasonTime = seasonCycleLength / 4;

        BetterWeatherSeasonData.Season season = getSeasonFromTime(seasonTime, seasonCycleLength);
        if (cachedSeason != season) {
            BetterWeather.seasonData.setSeason(season.toString());
            cachedSeason = season;
        }

        int perSeasonTime3rd = perSeasonTime / 3;

        int seasonOffset = perSeasonTime * season.ordinal();

        if (seasonTime < seasonOffset + perSeasonTime3rd)
            return season.getStart();
        else if (seasonTime < seasonOffset + (perSeasonTime3rd * 2))
            return season.getMid();
        else {
            return season.getEnd();
        }
    }

    public static BetterWeatherSeasonData.Season getSeasonFromTime(int seasonTime, int seasonCycleLength) {
        int perSeasonTime = seasonCycleLength / 4;

        if (seasonTime < perSeasonTime) {
            return BetterWeatherSeasonData.Season.SPRING;
        } else if (seasonTime < perSeasonTime * 2) {
            return BetterWeatherSeasonData.Season.SUMMER;
        } else if (seasonTime < perSeasonTime * 3) {
            return BetterWeatherSeasonData.Season.AUTUMN;
        } else
            return BetterWeatherSeasonData.Season.WINTER;
    }
}