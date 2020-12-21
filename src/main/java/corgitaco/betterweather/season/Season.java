package corgitaco.betterweather.season;

import net.minecraft.util.Util;

import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

public class Season {

    public static final Season SPRING = new Season(SubSeason.SPRING_START, SubSeason.SPRING_MID, SubSeason.SPRING_END);
    public static final Season SUMMER = new Season(SubSeason.SUMMER_START, SubSeason.SUMMER_MID, SubSeason.SUMMER_END);
    public static final Season AUTUMN = new Season(SubSeason.AUTUMN_START, SubSeason.AUTUMN_MID, SubSeason.AUTUMN_END);
    public static final Season WINTER = new Season(SubSeason.WINTER_START, SubSeason.WINTER_MID, SubSeason.WINTER_END);


    public static Map<String, Season> SEASON_MAP = Util.make((new TreeMap<>()), (map) -> {
        map.put(BWSeasons.SeasonVal.SPRING.toString(), SPRING);
        map.put(BWSeasons.SeasonVal.SUMMER.toString(), SUMMER);
        map.put(BWSeasons.SeasonVal.AUTUMN.toString(), AUTUMN);
        map.put(BWSeasons.SeasonVal.WINTER.toString(), WINTER);
    });

    public static Map<String, SubSeason> SUB_SEASON_MAP = Util.make((new TreeMap<>()), (map) -> {
        map.put(BWSeasons.SubSeasonVal.SPRING_START.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.SPRING.toString()).getStart());
        map.put(BWSeasons.SubSeasonVal.SPRING_MID.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.SPRING.toString()).getMid());
        map.put(BWSeasons.SubSeasonVal.SPRING_END.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.SPRING.toString()).getEnd());
        map.put(BWSeasons.SubSeasonVal.SUMMER_START.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.SUMMER.toString()).getStart());
        map.put(BWSeasons.SubSeasonVal.SUMMER_MID.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.SUMMER.toString()).getMid());
        map.put(BWSeasons.SubSeasonVal.SUMMER_END.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.SUMMER.toString()).getEnd());
        map.put(BWSeasons.SubSeasonVal.AUTUMN_START.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.AUTUMN.toString()).getStart());
        map.put(BWSeasons.SubSeasonVal.AUTUMN_MID.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.AUTUMN.toString()).getMid());
        map.put(BWSeasons.SubSeasonVal.AUTUMN_END.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.AUTUMN.toString()).getEnd());
        map.put(BWSeasons.SubSeasonVal.WINTER_START.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.WINTER.toString()).getStart());
        map.put(BWSeasons.SubSeasonVal.WINTER_MID.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.WINTER.toString()).getMid());
        map.put(BWSeasons.SubSeasonVal.WINTER_END.toString(), SEASON_MAP.get(BWSeasons.SeasonVal.WINTER.toString()).getEnd());
    });


    public static Season getSeasonFromEnum(BWSeasons.SeasonVal season) {
        return SEASON_MAP.get(season.toString());
    }

    public static SubSeason getSubSeasonFromEnum(BWSeasons.SubSeasonVal season) {
        return SUB_SEASON_MAP.get(season.toString());
    }


    private final SubSeason start;
    private final SubSeason mid;
    private final SubSeason end;

    public Season(SubSeason start, SubSeason mid, SubSeason end) {
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

    public boolean containsSubSeason(BWSeasons.SubSeasonVal subSeason) {
        return subSeason == start.getStageVal() || subSeason == mid.getStageVal() || subSeason == end.getStageVal();
    }

    public static class SubSeason {

        public static final SubSeason SPRING_START = new SubSeason(0, 0.5, -0.5, new Color(51, 97, 50).getRGB(), new Color(51, 97, 50).getRGB(), 0.5);
        public static final SubSeason SPRING_MID = new SubSeason(0, 0.5, -0.5, new Color(41, 87, 2).getRGB(), new Color(41, 87, 2).getRGB(), 0.5);
        public static final SubSeason SPRING_END = new SubSeason(0, 0.5, -0.5, new Color(20, 87, 2).getRGB(), new Color(20, 87, 2).getRGB(), 0.5);

        public static final SubSeason SUMMER_START = new SubSeason(0.5, 0, 0, new Color(165, 42, 42).getRGB(), new Color(165, 42, 42).getRGB(), 0.5);
        public static final SubSeason SUMMER_MID = new SubSeason(0.5, 0, 0, new Color(165, 255, 42).getRGB(), new Color(165, 42, 42).getRGB(), 0.5);
        public static final SubSeason SUMMER_END = new SubSeason(0.5, 0, 0, new Color(165, 42, 42).getRGB(), new Color(165, 42, 42).getRGB(), 0.5);

        public static final SubSeason AUTUMN_START = new SubSeason(-0.1, 0.2, 0, new Color(155, 103, 60).getRGB(), new Color(155, 103, 60).getRGB(), 0.5);
        public static final SubSeason AUTUMN_MID = new SubSeason(-0.1, 0.2, 0, new Color(155, 103, 60).getRGB(), new Color(155, 103, 60).getRGB(), 0.5);
        public static final SubSeason AUTUMN_END = new SubSeason(-0.1, 0.2, 0, new Color(155, 103, 60).getRGB(), new Color(155, 103, 60).getRGB(), 0.5);

        public static final SubSeason WINTER_START = new SubSeason(-0.5, 0.3, 0.4, new Color(165, 42, 42).getRGB(), new Color(165, 42, 42).getRGB(), 0.5);
        public static final SubSeason WINTER_MID = new SubSeason(-0.5, 0.3, 0.4, new Color(165, 42, 255).getRGB(), new Color(165, 42, 42).getRGB(), 0.5);
        public static final SubSeason WINTER_END = new SubSeason(-0.5, 0.3, 0.4, new Color(165, 42, 42).getRGB(), new Color(165, 42, 42).getRGB(), 0.5);


        private final double tempModifier;
        private final double downfallModifier;
        private final double cropGrowthChanceModifier;
        private final int targetFoliageColor;
        private final int targetGrassColor;
        private final double seasonBlendStrength;

        public SubSeason(double tempModifier, double downfallModifier, double cropGrowthChanceModifier, int targetFoliageColor, int targetGrassColor, double seasonBlendStrength) {
            this.tempModifier = tempModifier;
            this.downfallModifier = downfallModifier;
            this.cropGrowthChanceModifier = cropGrowthChanceModifier;
            this.targetFoliageColor = targetFoliageColor;
            this.targetGrassColor = targetGrassColor;
            this.seasonBlendStrength = seasonBlendStrength;
        }

        private String stage;

        public void setStageVal(BWSeasons.SubSeasonVal val) {
            stage = val.toString();
        }

        public BWSeasons.SubSeasonVal getStageVal() {
            return BWSeasons.SubSeasonVal.valueOf(stage);
        }

        public double getTempModifier() {
            return tempModifier;
        }

        public double getDownfallModifier() {
            return downfallModifier;
        }

        public double getCropGrowthChanceModifier() {
            return cropGrowthChanceModifier;
        }

        public int getFoliageTarget() {
            return targetFoliageColor;
        }

        public int getTargetGrassColor() {
            return targetGrassColor;
        }

        public double getSeasonBlendStrength() {
            return seasonBlendStrength;
        }
    }
}
