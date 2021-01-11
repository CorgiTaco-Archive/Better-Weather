package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class BetterWeatherEventData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":weather_event_data";

    private BetterWeatherID event = WeatherEventSystem.CLEAR;
    private boolean isWeatherForced;
    private boolean modified;

    public BetterWeatherEventData() {
        super(DATA_NAME);
    }

    public BetterWeatherEventData(String s) {
        super(s);
    }

    @Override
    public void read(CompoundNBT nbt) {
        setEvent(nbt.getString("Event"));
        setWeatherForced(nbt.getBoolean("Forced"));
        setModified(nbt.getBoolean("Modified"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putString("Event", event.toString());
        compound.putBoolean("Forced", isWeatherForced);
        compound.putBoolean("Modified", modified);
        return compound;
    }

    @Deprecated
    public boolean isAcidRain() {
        return this.event == WeatherEventSystem.ACID_RAIN;
    }

    @Deprecated
    public boolean isBlizzard() {
        return this.event == WeatherEventSystem.BLIZZARD;
    }

    public String getEventString() {
        return event.toString();
    }

    public BetterWeatherID getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = new BetterWeatherID(event);
        markDirty();
        WeatherData.currentWeatherEvent = WeatherEventSystem.WEATHER_EVENTS.get(this.event);
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

    public static BetterWeatherEventData get(IWorld world) {
        if (!(world instanceof ServerWorld))
            return new BetterWeatherEventData();
        ServerWorld overWorld = ((ServerWorld) world).getWorld().getServer().getWorld(World.OVERWORLD);
        DimensionSavedDataManager data = overWorld.getSavedData();
        BetterWeatherEventData weatherData = data.getOrCreate(BetterWeatherEventData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new BetterWeatherEventData();
            data.set(weatherData);
        }

        return weatherData;
    }
}
