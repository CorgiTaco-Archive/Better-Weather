package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class BetterWeatherGeneralData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":data";
    private static BetterWeatherGeneralData CLIENT_CACHE = new BetterWeatherGeneralData();
    private boolean usingSeasons;

    public BetterWeatherGeneralData() {
        super(DATA_NAME);
    }

    public BetterWeatherGeneralData(String s) {
        super(s);
    }

    public static BetterWeatherGeneralData get(IWorld world) {
        if (!(world instanceof ServerWorld)) {
            return CLIENT_CACHE;
        }
        ServerWorld overWorld = ((ServerWorld) world).getWorld().getServer().getWorld(World.OVERWORLD);
        DimensionSavedDataManager data = overWorld.getSavedData();
        BetterWeatherGeneralData weatherData = data.getOrCreate(BetterWeatherGeneralData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new BetterWeatherGeneralData();
            data.set(weatherData);
        }

        return weatherData;
    }

    @Override
    public void read(CompoundNBT nbt) {
        setUsingSeasons(nbt.getBoolean("usingseasons"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putBoolean("usingseasons", usingSeasons);
        return compound;
    }

    public boolean isUsingSeasons() {
        return usingSeasons;
    }

    public void setUsingSeasons(boolean seasonTime) {
        this.usingSeasons = seasonTime;
        markDirty();
    }
}
