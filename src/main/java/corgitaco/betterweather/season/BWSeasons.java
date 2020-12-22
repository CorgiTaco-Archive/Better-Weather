package corgitaco.betterweather.season;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.SeasonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BWSeasons {

    public static SubSeasonVal cachedSubSeason = SubSeasonVal.SPRING_START;
    public static SeasonVal cachedSeason = SeasonVal.SPRING;

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

        SubSeasonVal subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength()).getStageVal();

        if (cachedSubSeason != subSeason) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
        }

        if (BetterWeather.seasonData.getSeasonTime() % 25 == 0 || BetterWeather.seasonData.isForced())
            NetworkHandler.sendTo((ServerPlayerEntity) player, new SeasonPacket(BetterWeather.seasonData.getSeasonTime(), BetterWeather.SEASON_CYCLE_LENGTH));

        if (BetterWeather.seasonData.isForced())
            BetterWeather.seasonData.setForced(false);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSeason() {
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();

        SubSeasonVal subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength()).getStageVal();

        if (cachedSubSeason != subSeason) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
            Minecraft minecraft = Minecraft.getInstance();
            cachedSubSeason = subSeason;
            minecraft.worldRenderer.loadRenderers();
        }
    }


    public static Season.SubSeason getSubSeasonFromTime(int seasonTime, int seasonCycleLength) {
        int perSeasonTime = seasonCycleLength / 4;

        SeasonVal seasonVal = getSeasonFromTime(seasonTime, seasonCycleLength);
        if (cachedSeason != seasonVal) {
            BetterWeather.seasonData.setSeason(seasonVal.toString());
            cachedSeason = seasonVal;
        }

        int perSeasonTime3rd = perSeasonTime / 3;

        int seasonOffset = perSeasonTime * seasonVal.ordinal();

        if (seasonTime < seasonOffset + perSeasonTime3rd)
            return Season.getSeasonFromEnum(seasonVal).getStart();
        else if (seasonTime < seasonOffset + (perSeasonTime3rd * 2))
            return Season.getSeasonFromEnum(seasonVal).getMid();
        else {
            return Season.getSeasonFromEnum(seasonVal).getEnd();
        }
    }

    public static int getTimeInCycleForSubSeason(SubSeasonVal subSeasonVal, int seasonCycleLength) {
        int perSubSeasonLength = seasonCycleLength / (SubSeasonVal.values().length);
        return perSubSeasonLength * subSeasonVal.ordinal();
    }

    public static SeasonVal getSeasonFromTime(int seasonTime, int seasonCycleLength) {
        int perSeasonTime = seasonCycleLength / 4;

        if (seasonTime < perSeasonTime) {
            return SeasonVal.SPRING;
        } else if (seasonTime < perSeasonTime * 2) {
            return SeasonVal.SUMMER;
        } else if (seasonTime < perSeasonTime * 3) {
            return SeasonVal.AUTUMN;
        } else
            return SeasonVal.WINTER;
    }

    public enum SeasonVal {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER;
    }

    public enum SubSeasonVal {
        SPRING_START,
        SPRING_MID,
        SPRING_END,

        SUMMER_START,
        SUMMER_MID,
        SUMMER_END,

        AUTUMN_START,
        AUTUMN_MID,
        AUTUMN_END,

        WINTER_START,
        WINTER_MID,
        WINTER_END;
    }
}