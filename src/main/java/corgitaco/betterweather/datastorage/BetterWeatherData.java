package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class BetterWeatherData extends WorldSavedData {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":weather_event_data";

    private String event;
    private boolean isWeatherForced;

    public BetterWeatherData() {
        super(DATA_NAME);
    }

    public BetterWeatherData(String s) {
        super(s);
    }

    @Override
    public void read(CompoundNBT nbt) {
        setEvent(nbt.getString("Event"));
        setWeatherForced(nbt.getBoolean("Forced"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putString("Event", event);
        compound.putBoolean("Forced", isWeatherForced);
        return compound;
    }

    @Deprecated
    public boolean isAcidRain() {
        return BetterWeather.WeatherEvent.valueOf(this.event) == BetterWeather.WeatherEvent.ACID_RAIN;
    }

    @Deprecated
    public boolean isBlizzard() {
        return BetterWeather.WeatherEvent.valueOf(this.event) == BetterWeather.WeatherEvent.BLIZZARD;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
        markDirty();
    }

    public void setEvent(BetterWeather.WeatherEvent event) {
        this.event = event.toString();
        markDirty();
    }


    public boolean isWeatherForced() {
        return isWeatherForced;
    }

    public void setWeatherForced(boolean weatherForced) {
        isWeatherForced = weatherForced;
        markDirty();
    }

    public static BetterWeatherData get(IWorld world) {
        if (!(world instanceof ServerWorld))
            return new BetterWeatherData();
        ServerWorld overWorld = ((ServerWorld) world).getWorld().getServer().getWorld(World.OVERWORLD);
        DimensionSavedDataManager data = overWorld.getSavedData();
        BetterWeatherData weatherData = data.getOrCreate(BetterWeatherData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new BetterWeatherData();
            data.set(weatherData);
        }

        return weatherData;
    }
}
