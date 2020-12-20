package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.awt.*;

public class BetterWeatherSeasonData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":season_data";

    private int colorModifier;
    private int seasonTime;
    private String season = Season.SPRING.toString();
    private String subseason;

    public BetterWeatherSeasonData() {
        super(DATA_NAME);
    }

    public BetterWeatherSeasonData(String s) {
        super(s);
    }

    @Override
    public void read(CompoundNBT nbt) {
        setSeasonTime(nbt.getInt("seasontime"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("seasontime", seasonTime);
        return compound;
    }

    public int getColorModifier() {
        return this.colorModifier;
    }

    public int getSeasonTime() {
        return seasonTime;
    }

    public void setColorModifier(int colorModifier) {
        this.colorModifier = colorModifier;
        markDirty();
    }

    public void setSeasonTime(int seasonTime) {
        this.seasonTime = seasonTime;
        markDirty();
    }

    public Season getSeason() {
        return Season.valueOf(season);
    }

    public void setSeason(String season) {
        this.season = season;
        setSubSeason();
        markDirty();
    }

    public void setSubSeason() {
        this.subseason = getSeason().getSubSeasonFromTime(seasonTime).toString();
        BetterWeather.LOGGER.info("SEASON: " + this.subseason);
    }

    public SubSeason getSubSeason() {
        return SubSeason.valueOf(subseason == null ? getSeason().getSubSeasonFromTime(seasonTime).toString() : subseason);
    }

    public static BetterWeatherSeasonData get(IWorld world) {
        if (!(world instanceof ServerWorld))
            return new BetterWeatherSeasonData();
        ServerWorld overWorld = ((ServerWorld) world).getWorld().getServer().getWorld(World.OVERWORLD);
        DimensionSavedDataManager data = overWorld.getSavedData();
        BetterWeatherSeasonData weatherData = data.getOrCreate(BetterWeatherSeasonData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new BetterWeatherSeasonData();
            data.set(weatherData);
        }

        return weatherData;
    }

    public enum Season {
        WINTER(SubSeason.WINTER_START, SubSeason.WINTER_MID, SubSeason.WINTER_END),
        SPRING(SubSeason.SPRING_START, SubSeason.SPRING_MID, SubSeason.SPRING_END),
        SUMMER(SubSeason.SUMMER_START, SubSeason.SUMMER_MID, SubSeason.SUMMER_END),
        AUTUMN(SubSeason.AUTUMN_START, SubSeason.AUTUMN_MID, SubSeason.AUTUMN_END);

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

        public SubSeason getSubSeasonFromTime(int seasonTime) {
            double seasonTime3rd = BetterWeather.SEASON_LENGTH * 0.3;

            if (seasonTime < seasonTime3rd)
                return getStart();
            else if (seasonTime < seasonTime3rd * 2)
                return getMid();
            else {
                return getEnd();
            }
        }
    }

    public enum SubSeason {
        WINTER_START(-0.5, 0.3, 0.4, new Color(165, 42, 42), new Color(165, 42, 42)),
        WINTER_MID(-0.5, 0.3, 0.4, new Color(165, 42, 42), new Color(165, 42, 42)),
        WINTER_END(-0.5, 0.3, 0.4, new Color(165, 42, 42), new Color(165, 42, 42)),

        SPRING_START(0, 0.5, -0.5, new Color(51, 97, 50), new Color(51, 97, 50)),
        SPRING_MID(0, 0.5, -0.5, new Color(41, 87, 2), new Color(41, 87, 2)),
        SPRING_END(0, 0.5, -0.5, new Color(20, 87, 2), new Color(20, 87, 2)),

        SUMMER_START(0.5, 0, 0, new Color(165, 42, 42), new Color(165, 42, 42)),
        SUMMER_MID(0.5, 0, 0, new Color(165, 42, 42), new Color(165, 42, 42)),
        SUMMER_END(0.5, 0, 0, new Color(165, 42, 42), new Color(165, 42, 42)),

        AUTUMN_START(-0.1, 0.2, 0, new Color(155, 103, 60), new Color(155, 103, 60)),
        AUTUMN_MID(-0.1, 0.2, 0, new Color(155, 103, 60), new Color(155, 103, 60)),
        AUTUMN_END(-0.1, 0.2, 0, new Color(155, 103, 60), new Color(155, 103, 60));

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