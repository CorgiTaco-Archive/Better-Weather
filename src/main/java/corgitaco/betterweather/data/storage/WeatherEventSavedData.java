package corgitaco.betterweather.data.storage;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class WeatherEventSavedData extends WorldSavedData {
    public static String DATA_NAME = new ResourceLocation(BetterWeather.MOD_ID, "weather_event_data").toString();
    private boolean isWeatherForced;
    private boolean modified;
    private String event;

    public WeatherEventSavedData() {
        super(DATA_NAME);
    }

    public WeatherEventSavedData(String s) {
        super(s);
    }

    private static WeatherEventSavedData clientCache = new WeatherEventSavedData();
    private static ClientWorld worldCache = null;

    public static WeatherEventSavedData get(IWorld world) {
        if (!(world instanceof ServerWorld)) {
            if (worldCache != world) {
                worldCache = (ClientWorld) world;
                clientCache = new WeatherEventSavedData();
            }
            return clientCache;
        }
        DimensionSavedDataManager data = ((ServerWorld) world).getSavedData();
        WeatherEventSavedData weatherData = data.getOrCreate(WeatherEventSavedData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new WeatherEventSavedData();
            data.set(weatherData);
        }

        return weatherData;
    }

    @Override
    public void read(CompoundNBT nbt) {
        setEvent(nbt.getString("Event"));
        setWeatherForced(nbt.getBoolean("Forced"));
        setModified(nbt.getBoolean("Modified"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putString("Event", event);
        compound.putBoolean("Forced", isWeatherForced);
        compound.putBoolean("Modified", modified);
        return compound;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
        markDirty();
    }

    public boolean isWeatherForced() {
        return isWeatherForced;
    }

    public void setWeatherForced(boolean weatherForced) {
        isWeatherForced = weatherForced;
        markDirty();
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
        markDirty();
    }
}