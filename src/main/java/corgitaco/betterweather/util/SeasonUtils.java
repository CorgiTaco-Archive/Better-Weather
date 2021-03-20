package corgitaco.betterweather.util;

import corgitaco.betterweather.api.season.Season;

public class SeasonUtils {

    public static int getPhaseLength(int seasonLength) {
        return seasonLength / Season.Phase.values().length;
    }

    public static int getSubSeasonLength(int seasonCycleLength) {
        return seasonCycleLength / (Season.Key.values().length);
    }

    public static int getTimeInCycleForSeason(Season.Key subSeasonVal, int seasonCycleLength) {
        int perSubSeasonLength = getSubSeasonLength(seasonCycleLength);
        return perSubSeasonLength * subSeasonVal.ordinal();
    }


    public static int getTimeInCycleForSeasonAndPhase(Season.Key subSeasonVal, Season.Phase phase, int seasonCycleLength) {
        int timeInCycleForSeason = getTimeInCycleForSeason(subSeasonVal, seasonCycleLength);
        int phaseTime = getSubSeasonLength(seasonCycleLength) / Season.Phase.values().length;
        return timeInCycleForSeason + (phaseTime * phase.ordinal());
    }


    public static Season.Key getSeasonFromTime(int seasonTimeInCycle, int seasonCycleLength) {
        int seasonLength = seasonCycleLength / 4;

        if (seasonTimeInCycle < seasonLength) {
            return Season.Key.SPRING;
        } else if (seasonTimeInCycle < seasonLength * 2) {
            return Season.Key.SUMMER;
        } else if (seasonTimeInCycle < seasonLength * 3) {
            return Season.Key.AUTUMN;
        } else
            return Season.Key.WINTER;
    }
}
