package corgitaco.betterweather.season;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.SeasonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

public class BWSeasons {

    public static BWSeasons.SubSeason cachedSubSeason = BWSeasons.SubSeason.SPRING_START;
    public static BWSeasons.Season cachedSeason = BWSeasons.Season.SPRING;

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

        BWSeasons.SubSeason subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength());

        if (cachedSubSeason != subSeason) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
        }


        NetworkHandler.sendTo((ServerPlayerEntity) player, new SeasonPacket(BetterWeather.seasonData.getSeasonTime(), BetterWeather.SEASON_CYCLE_LENGTH));
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSeason() {
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();

        BWSeasons.SubSeason subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength());

        if (cachedSubSeason != subSeason) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
            Minecraft minecraft = Minecraft.getInstance();
            cachedSubSeason = subSeason;
            minecraft.worldRenderer.loadRenderers();
        }
    }

    public static BWSeasons.SubSeason getSubSeasonFromTime(int seasonTime, int seasonCycleLength) {
        int perSeasonTime = seasonCycleLength / 4;

        BWSeasons.Season season = getSeasonFromTime(seasonTime, seasonCycleLength);
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

    public static BWSeasons.Season getSeasonFromTime(int seasonTime, int seasonCycleLength) {
        int perSeasonTime = seasonCycleLength / 4;

        if (seasonTime < perSeasonTime) {
            return BWSeasons.Season.SPRING;
        } else if (seasonTime < perSeasonTime * 2) {
            return BWSeasons.Season.SUMMER;
        } else if (seasonTime < perSeasonTime * 3) {
            return BWSeasons.Season.AUTUMN;
        } else
            return BWSeasons.Season.WINTER;
    }

    public enum Season {
        SPRING(SubSeason.SPRING_START, SubSeason.SPRING_MID, SubSeason.SPRING_END),
        SUMMER(SubSeason.SUMMER_START, SubSeason.SUMMER_MID, SubSeason.SUMMER_END),
        AUTUMN(SubSeason.AUTUMN_START, SubSeason.AUTUMN_MID, SubSeason.AUTUMN_END),
        WINTER(SubSeason.WINTER_START, SubSeason.WINTER_MID, SubSeason.WINTER_END);


        private final SubSeason start;
        private final SubSeason mid;
        private final SubSeason end;

        Season(SubSeason start, SubSeason mid, SubSeason end) {
            this.start = start;
            this.mid = mid;
            this.end = end;
        }

        public SubSeason getStart() {
            return start;
        }

        public SubSeason getMid() {
            return mid;
        }

        public SubSeason getEnd() {
            return end;
        }
    }

    public enum SubSeason {
        SPRING_START(0, 0.5, -0.5, new Color(51, 97, 50), new Color(51, 97, 50)),
        SPRING_MID(0, 0.5, -0.5, new Color(41, 87, 2), new Color(41, 87, 2)),
        SPRING_END(0, 0.5, -0.5, new Color(20, 87, 2), new Color(20, 87, 2)),

        SUMMER_START(0.5, 0, 0, new Color(165, 42, 42), new Color(165, 42, 42)),
        SUMMER_MID(0.5, 0, 0, new Color(165, 255, 42), new Color(165, 42, 42)),
        SUMMER_END(0.5, 0, 0, new Color(165, 42, 42), new Color(165, 42, 42)),

        AUTUMN_START(-0.1, 0.2, 0, new Color(155, 103, 60), new Color(155, 103, 60)),
        AUTUMN_MID(-0.1, 0.2, 0, new Color(155, 103, 60), new Color(155, 103, 60)),
        AUTUMN_END(-0.1, 0.2, 0, new Color(155, 103, 60), new Color(155, 103, 60)),

        WINTER_START(-0.5, 0.3, 0.4, new Color(165, 42, 42), new Color(165, 42, 42)),
        WINTER_MID(-0.5, 0.3, 0.4, new Color(165, 42, 255), new Color(165, 42, 42)),
        WINTER_END(-0.5, 0.3, 0.4, new Color(165, 42, 42), new Color(165, 42, 42));


        private final double tempModifier;
        private final double downfallModifier;
        private final double cropGrowthChanceModifier;
        private final Color foliageTarget;
        private final Color grassTarget;

        SubSeason(double tempModifier, double downfallModifier, double cropGrowthChanceModifier, Color targetFoliageColor, Color grassTarget) {
            this.tempModifier = tempModifier;
            this.downfallModifier = downfallModifier;
            this.cropGrowthChanceModifier = cropGrowthChanceModifier;
            this.foliageTarget = targetFoliageColor;
            this.grassTarget = grassTarget;
        }

        public double getDownfallModifier() {
            return downfallModifier;
        }

        public double getTempModifier() {
            return tempModifier;
        }

        public double getCropGrowthChanceModifier() {
            return cropGrowthChanceModifier;
        }

        public Color getFoliageTarget() {
            return foliageTarget;
        }

        public Color getGrassTarget() {
            return grassTarget;
        }
    }

}