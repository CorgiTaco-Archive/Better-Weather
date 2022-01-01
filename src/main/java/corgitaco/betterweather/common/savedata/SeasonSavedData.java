package corgitaco.betterweather.common.savedata;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class SeasonSavedData extends WorldSavedData {
    private static final String DATA_NAME = new ResourceLocation(BetterWeather.MOD_ID, "season_data").toString();

    private static final String YEAR_LENGTH_KEY = "yearLength";

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
        DimensionSavedDataManager data = ((ServerWorld) world).getDataStorage();
        SeasonSavedData weatherData = data.computeIfAbsent(SeasonSavedData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new SeasonSavedData();
            data.set(weatherData);
        }

        return weatherData;
    }

    @Override
    public void load(CompoundNBT nbt) {
        setYearLength(nbt.getInt(YEAR_LENGTH_KEY));
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putInt(YEAR_LENGTH_KEY, yearLength);
        return compound;
    }

    public int getYearLength() {
        return yearLength;
    }

    public void setYearLength(int yearLength) {
        this.yearLength = yearLength;
    }
}