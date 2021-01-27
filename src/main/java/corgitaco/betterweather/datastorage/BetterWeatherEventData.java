package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class BetterWeatherEventData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":weather_event_data";
    private static final BetterWeatherEventData CLIENT_CACHE = new BetterWeatherEventData();
    private BetterWeatherID event = WeatherEventSystem.CLEAR;
    private boolean isWeatherForced;
    private boolean modified;

    public BetterWeatherEventData() {
        super(DATA_NAME);
    }

    public BetterWeatherEventData(String s) {
        super(s);
    }

    public static BetterWeatherEventData get(IWorld world) {
        if (!(world instanceof ServerWorld))
            return CLIENT_CACHE;
        ServerWorld overWorld = ((ServerWorld) world).getWorld().getServer().getWorld(World.OVERWORLD);
        DimensionSavedDataManager data = overWorld.getSavedData();
        BetterWeatherEventData weatherData = data.getOrCreate(BetterWeatherEventData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new BetterWeatherEventData();
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

    public BetterWeatherID getEventID() {
        return event;
    }

    public WeatherEvent getEvent() {
        WeatherEvent weatherEvent = WeatherEventSystem.WEATHER_EVENTS.get(this.event);
        if (weatherEvent == null) {
            weatherEvent = WeatherEventSystem.WEATHER_EVENTS.get(WeatherEventSystem.CLEAR);
            BetterWeather.LOGGER.error("Getting the current Weather Event saved data was null for this world, restoring data by defaulting to clear...");
        }
        return weatherEvent;
    }

    public void setEvent(String event) {
        this.event = new BetterWeatherID(event);
        markDirty();
        WeatherEvent currentWeatherEvent = WeatherEventSystem.WEATHER_EVENTS.get(this.event);
        if (currentWeatherEvent == null) {
            WeatherData.currentWeatherEvent = WeatherEventSystem.WEATHER_EVENTS.get(WeatherEventSystem.CLEAR);
            BetterWeather.LOGGER.error("Setting the current Weather Event saved data was null for this world, restoring data by defaulting to clear...");
        }
        else
            WeatherData.currentWeatherEvent = currentWeatherEvent;
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
