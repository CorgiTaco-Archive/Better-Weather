package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.SeasonData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class SeasonSavedData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":season_data";
    private static SeasonSavedData CLIENT_CACHE = new SeasonSavedData();
    private int seasonTime;
    private int seasonCycleLength;
    private String season = SeasonData.SeasonKey.SPRING.toString();
    private boolean isForced;

    public SeasonSavedData() {
        super(DATA_NAME);
    }

    public SeasonSavedData(String s) {
        super(s);
    }

    private static ClientWorld worldCache = null;

    public static SeasonSavedData get(IWorld world) {
        if (!(world instanceof ServerWorld)) {
            if (worldCache != world) {
                worldCache = (ClientWorld) world;
                CLIENT_CACHE = new SeasonSavedData();
            }
            return CLIENT_CACHE;
        }
        DimensionSavedDataManager data = ((ServerWorld) world).getSavedData();
        SeasonSavedData weatherData = data.getOrCreate(SeasonSavedData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new SeasonSavedData();
            data.set(weatherData);
        }

        return weatherData;
    }

    @Override
    public void read(CompoundNBT nbt) {
        setSeasonTime(nbt.getInt("seasontime"));
        setSeasonCycleLength(nbt.getInt("seasoncyclelength"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("seasontime", seasonTime);
        compound.putInt("seasoncyclelength", seasonCycleLength);
        return compound;
    }

    public int getSeasonTime() {
        return seasonTime;
    }

    public void setSeasonTime(int seasonTime) {
        this.seasonTime = seasonTime;
        markDirty();
    }

    public SeasonData.SeasonKey getSeason() {
        return SeasonData.SeasonKey.valueOf(season);
    }

    public int getSeasonCycleLength() {
        return seasonCycleLength;
    }

    public void setSeasonCycleLength(int seasonCycleLength) {
        this.seasonCycleLength = seasonCycleLength;
    }

    public boolean isForced() {
        return isForced;
    }

    public void setForced(boolean forced) {
        isForced = forced;
    }
}
