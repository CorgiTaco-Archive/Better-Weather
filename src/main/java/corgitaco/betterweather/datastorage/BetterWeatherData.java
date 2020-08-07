package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class BetterWeatherData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":weather_data";

    private boolean acidRain;

    public BetterWeatherData() {
        super(DATA_NAME);
    }

    public BetterWeatherData(String s) {
        super(s);
    }

    @Override
    public void read(CompoundNBT nbt) {
        setAcidRain(nbt.getBoolean("AcidRain"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putBoolean("AcidRain", acidRain);
        return compound;
    }

    public boolean isAcidRain() {
        return this.acidRain;
    }

    public void setAcidRain(boolean acidRain) {
        this.acidRain = acidRain;
        markDirty();
    }


    public static BetterWeatherData get(IWorld world) {
        if (!(world instanceof ServerWorld))
            return new BetterWeatherData();
        ServerWorld overWorld = world.getWorld().getServer().getWorld(World.field_234918_g_);
        DimensionSavedDataManager data = overWorld.getSavedData();
        BetterWeatherData weatherData = data.getOrCreate(BetterWeatherData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new BetterWeatherData();
            data.set(weatherData);
        }

        return weatherData;
    }


}
