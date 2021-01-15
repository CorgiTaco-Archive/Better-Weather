package corgitaco.betterweather.api;

import javax.annotation.Nullable;

/**
 * Contains information in regards to the current subseason & season
 */
public class SeasonData {

    @Nullable public static SubSeasonVal currentSubSeason = SeasonData.SubSeasonVal.SPRING_START;
    @Nullable public static SeasonVal currentSeason = SeasonData.SeasonVal.SPRING;


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
