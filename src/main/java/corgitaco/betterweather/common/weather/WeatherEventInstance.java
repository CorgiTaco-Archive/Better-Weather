package corgitaco.betterweather.common.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.weather.WeatherEvent;

import java.util.Map;

public class WeatherEventInstance {

    public static final Codec<WeatherEventInstance> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.STRING.fieldOf("weatherEventKey").forGetter((weatherEventInstance) -> {
            return weatherEventInstance.weatherEventKey;
        }), Codec.LONG.fieldOf("scheduledDay").forGetter(weatherEventInstance -> {
            return weatherEventInstance.scheduledDay;
        }), Codec.BOOL.fieldOf("forced").forGetter(weatherEventInstance -> {
            return weatherEventInstance.forced;
        })).apply(builder, WeatherEventInstance::new);
    });


    private final String weatherEventKey;
    private long scheduledDay;
    private final boolean forced;

    public WeatherEventInstance(String weatherEventKey, long scheduledDay) {
        this(weatherEventKey, scheduledDay, false);
    }

    public WeatherEventInstance(String weatherEventKey, long scheduledDay, boolean forced) {
        this.weatherEventKey = weatherEventKey;
        this.scheduledDay = scheduledDay;
        this.forced = forced;
    }

    public String getWeatherEventKey() {
        return weatherEventKey;
    }

    public WeatherEvent getEvent(Map<String, WeatherEvent> events) {
        return events.get(weatherEventKey);
    }

    public long scheduledDay() {
        return scheduledDay;
    }

    public long getDaysUntil(long currentDay) {
        return this.scheduledDay - currentDay;
    }

    public boolean passed(long currentDay) {
        return this.scheduledDay - currentDay <= -1;
    }

    public boolean active(long currentDay) {
        return this.scheduledDay - currentDay == 0;
    }

    public void setScheduledDay(int scheduledDay) {
        this.scheduledDay = scheduledDay;
    }
}
