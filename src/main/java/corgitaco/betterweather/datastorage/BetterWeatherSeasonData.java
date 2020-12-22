package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.BWSeasonSystem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class BetterWeatherSeasonData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":season_data";

    private int seasonTime;
    private int seasonCycleLength;
    private String season = BWSeasonSystem.SeasonVal.SPRING.toString();
    private String subseason;
    private boolean isForced;

    public BetterWeatherSeasonData() {
        super(DATA_NAME);
    }

    public BetterWeatherSeasonData(String s) {
        super(s);
    }

    @Override
    public void read(CompoundNBT nbt) {
        setSeasonTime(nbt.getInt("seasontime"));
        setSeasonCycleLength(nbt.getInt("seasoncyclelength"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("seasontime", seasonTime);
        compound.putInt("seasoncyclelength", seasonTime);
        return compound;
    }

    public int getSeasonTime() {
        return seasonTime;
    }

    public void setSeasonTime(int seasonTime) {
        this.seasonTime = seasonTime;
        markDirty();
    }

    public BWSeasonSystem.SeasonVal getSeason() {
        return BWSeasonSystem.SeasonVal.valueOf(season);
    }

    public void setSeason(String season) {
        this.season = season;
        markDirty();
    }

    public int getSeasonCycleLength() {
        return seasonCycleLength;
    }

    public void setSeasonCycleLength(int seasonCycleLength) {
        this.seasonCycleLength = seasonCycleLength;
    }

    public void setSubseason(String subseason) {
        this.subseason = subseason;
    }

    public boolean isForced() {
        return isForced;
    }

    public void setForced(boolean forced) {
        isForced = forced;
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
}
