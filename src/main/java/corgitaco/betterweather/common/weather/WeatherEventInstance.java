package corgitaco.betterweather.common.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.weather.WeatherEvent;

import java.util.Map;

public class WeatherEventInstance {

    public static final Codec<WeatherEventInstance> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.STRING.fieldOf("weatherEventKey").forGetter((weatherEventInstance) -> {
            return weatherEventInstance.weatherEventKey;
        }), Codec.LONG.fieldOf("scheduledStartTime").forGetter(weatherEventInstance -> {
            return weatherEventInstance.scheduledDay;
        }), Codec.BOOL.fieldOf("forced").forGetter(weatherEventInstance -> {
            return weatherEventInstance.forced;
        }), Codec.INT.fieldOf("dayLocalStartTime").forGetter(weatherEventInstance -> {
            return weatherEventInstance.dayLocalStartTime;
        }), Codec.INT.fieldOf("eventLengthInTicks").forGetter(weatherEventInstance -> {
            return weatherEventInstance.eventLengthInTicks;
        })).apply(builder, WeatherEventInstance::new);
    });


    private final String weatherEventKey;
    private final long scheduledDay;
    private final boolean forced;
    private final int dayLocalStartTime;
    private final int eventLengthInTicks;

    public WeatherEventInstance(String weatherEventKey, long scheduledDay, int dayLocalStartTime, int eventLengthInTicks) {
        this(weatherEventKey, scheduledDay, false, dayLocalStartTime, eventLengthInTicks);
    }

    public WeatherEventInstance(String weatherEventKey, long scheduledDay, boolean forced, int dayLocalStartTime, int eventLengthInTicks) {
        this.weatherEventKey = weatherEventKey;
        this.scheduledDay = scheduledDay;
        this.forced = forced;
        this.dayLocalStartTime = dayLocalStartTime;
        this.eventLengthInTicks = eventLengthInTicks;
    }

    public String getWeatherEventKey() {
        return weatherEventKey;
    }

    public WeatherEvent getEvent(Map<String, WeatherEvent> events) {
        return events.get(weatherEventKey);
    }

    public long scheduledStartTime(long dayLength) {
        long scheduledDay = this.scheduledDay * dayLength;
        return scheduledDay + dayLocalStartTime;
    }

    public long getTimeUntil(long dayTime, long dayLength) {
        long scheduledDay = this.scheduledDay * dayLength;
        long scheduledStartTime = scheduledDay + dayLocalStartTime;

        return scheduledStartTime - dayTime;
    }

    public boolean eventPassed(long dayTime, long dayLength) {
        long scheduledDay = this.scheduledDay * dayLength;
        long scheduledStartTime = scheduledDay + dayLocalStartTime;
        long scheduledEndTime = scheduledStartTime + eventLengthInTicks;

        return scheduledEndTime - dayTime <= -1;
    }

    public boolean active(long dayTime, long dayLength) {
        long scheduledDay = this.scheduledDay * dayLength;
        long scheduledStartTime = scheduledDay + dayLocalStartTime;
        long scheduledEndTime = scheduledStartTime + eventLengthInTicks;

        return dayTime <= scheduledEndTime && dayTime >= scheduledStartTime;
    }

    public long getEndTime(long dayLength) {
        long scheduledDay = this.scheduledDay * dayLength;
        long scheduledStartTime = scheduledDay + dayLocalStartTime;

        return scheduledStartTime + eventLengthInTicks;
    }
}