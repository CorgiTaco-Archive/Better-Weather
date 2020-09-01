package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;


public class BetterWeatherData extends SavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":weather_data";

    private boolean acidRain;
    private boolean blizzard;


    public BetterWeatherData() {
        super(DATA_NAME);
    }

    public BetterWeatherData(String s) {
        super(s);
    }


    @Override
    public void load(CompoundTag nbt) {
        setAcidRain(nbt.getBoolean("AcidRain"));
        setBlizzard(nbt.getBoolean("Blizzard"));
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putBoolean("AcidRain", acidRain);
        compound.putBoolean("Blizzard", blizzard);
        return compound;
    }

    public boolean isAcidRain() {
        return this.acidRain;
    }

    public boolean isBlizzard() {
        return this.blizzard;
    }


    public void setAcidRain(boolean acidRain) {
        this.acidRain = acidRain;
        setDirty();
    }

    public void setBlizzard(boolean isBlizzard) {
        this.blizzard = isBlizzard;
        setDirty();
    }


    public static BetterWeatherData get(LevelAccessor world) {
        if (!(world instanceof ServerLevel))
            return new BetterWeatherData();
        ServerLevel overWorld = ((ServerLevel) world).getLevel().getServer().getLevel(Level.NETHER);
        DimensionDataStorage data = overWorld.getDataStorage();
        BetterWeatherData weatherData = data.get(BetterWeatherData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new BetterWeatherData();
            data.set(weatherData);
        }

        return weatherData;
    }


}
