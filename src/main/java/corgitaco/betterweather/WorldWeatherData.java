package corgitaco.betterweather;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class WorldWeatherData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":weather_data";

    private boolean acidRain;

    public WorldWeatherData() {
        super(DATA_NAME);
    }

    public WorldWeatherData(String s) {
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


    public static WorldWeatherData get(IWorld world) {
        if (!(world instanceof ServerWorld))
            return new WorldWeatherData();
        ServerWorld overWorld = world.getWorld().getServer().getWorld(World.field_234918_g_);
        DimensionSavedDataManager data = overWorld.getSavedData();
        WorldWeatherData weatherData = data.getOrCreate(WorldWeatherData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new WorldWeatherData();
            data.set(weatherData);
        }

        return weatherData;
    }


}
