package corgitaco.betterweather.common.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public class WeatherForecast {

    public static final Codec<WeatherForecast> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.list(WeatherEventInstance.CODEC).fieldOf("forecast").forGetter((weatherForecast) -> {
            return weatherForecast.forecast;
        }), Codec.LONG.fieldOf("lastCheckedGameTime").forGetter((weatherForecast -> {
            return weatherForecast.lastCheckedGameTime;
        }))).apply(builder, WeatherForecast::new);
    });

    private final List<WeatherEventInstance> forecast;
    private long lastCheckedGameTime;

    public WeatherForecast(List<WeatherEventInstance> forecast, long lastCheckedGameTime) {
        this.forecast = new ArrayList<>(forecast);
        this.lastCheckedGameTime = lastCheckedGameTime;
    }

    public List<WeatherEventInstance> getForecast() {
        return forecast;
    }

    public long getLastCheckedGameTime() {
        return lastCheckedGameTime;
    }

    public void setLastCheckedGameTime(long lastCheckedGameTime) {
        this.lastCheckedGameTime = lastCheckedGameTime;
    }
}