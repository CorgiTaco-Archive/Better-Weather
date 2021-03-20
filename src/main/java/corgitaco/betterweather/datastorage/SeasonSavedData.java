package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class SeasonSavedData extends WorldSavedData {
    private static final String DATA_NAME = BetterWeather.MOD_ID + ":season_data";

    //TODO: 1.17, Change keys to be "currentYearTime" & "yearLength"
    private static final String CURRENT_YEAR_TIME_KEY = "seasontime";
    private static final String YEAR_LENGTH_KEY = "seasoncyclelength";

    private int currentYearTime;
    private int yearLength;

    private static SeasonSavedData clientCache = new SeasonSavedData();
    private static ClientWorld worldCache = null;

    public SeasonSavedData() {
        super(DATA_NAME);
    }

    public SeasonSavedData(String s) {
        super(s);
    }

    public static SeasonSavedData get(IWorld world) {
        if (!(world instanceof ServerWorld)) {
            if (worldCache != world) {
                worldCache = (ClientWorld) world;
                clientCache = new SeasonSavedData();
            }
            return clientCache;
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
        setCurrentYearTime(nbt.getInt(CURRENT_YEAR_TIME_KEY));
        setYearLength(nbt.getInt(YEAR_LENGTH_KEY));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt(CURRENT_YEAR_TIME_KEY, currentYearTime);
        compound.putInt(YEAR_LENGTH_KEY, yearLength);
        return compound;
    }

    public int getCurrentYearTime() {
        return currentYearTime;
    }

    public void setCurrentYearTime(int currentYearTime) {
        this.currentYearTime = currentYearTime;
        markDirty();
    }

    public int getYearLength() {
        return yearLength;
    }

    public void setYearLength(int yearLength) {
        this.yearLength = yearLength;
    }
}
