package corgitaco.betterweather.common.savedata;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.common.weather.WeatherForecast;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

public class WeatherEventSavedData extends WorldSavedData {
    public static final String DATA_NAME = new ResourceLocation(BetterWeather.MOD_ID, "weather_event_data").toString();

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
        DimensionSavedDataManager data = ((ServerWorld) world).getDataStorage();
        WeatherEventSavedData weatherData = data.computeIfAbsent(WeatherEventSavedData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new WeatherEventSavedData();
            data.set(weatherData);
        }
        return weatherData;
    }

    @Nullable
    private WeatherForecast forecast;

    public WeatherEventSavedData(String name) {
        super(name);
    }

    public WeatherEventSavedData() {
        super(DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {
        forecast = WeatherForecast.CODEC.decode(NBTDynamicOps.INSTANCE, nbt.get("forecast")).result().get().getFirst();
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.put("forecast", WeatherForecast.CODEC.encodeStart(NBTDynamicOps.INSTANCE, forecast).result().get());
        return compound;
    }

    @Nullable
    public WeatherForecast getForecast() {
        return forecast;
    }

    public void setForecast(WeatherForecast forecast) {
        this.forecast = forecast;
        setDirty();
    }
}
