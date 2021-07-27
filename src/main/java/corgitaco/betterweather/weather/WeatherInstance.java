package corgitaco.betterweather.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundNBT;

public class WeatherInstance {

    public static final Codec<WeatherInstance> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.STRING.fieldOf("event").forGetter((weatherInstance) -> {
            return weatherInstance.weatherEvent;
        }), Codec.LONG.fieldOf("timeUntilEvent").forGetter((weatherInstance) -> {
            return weatherInstance.timeUntilEvent;
        }), Codec.INT.fieldOf("eventTime").forGetter((weatherInstance) -> {
            return weatherInstance.eventTime;
        })).apply(builder, WeatherInstance::new);
    });


    private final String weatherEvent;
    private long timeUntilEvent;
    private int eventTime;

    public WeatherInstance(String weatherEvent, long timeUntilEvent, int eventTime) {
        this.weatherEvent = weatherEvent;
        this.timeUntilEvent = timeUntilEvent;
        this.eventTime = eventTime;
    }

    public void setTimeUntilEvent(long timeUntilEvent) {
        this.timeUntilEvent = timeUntilEvent;
    }

    public void setEventTime(int eventTime) {
        this.eventTime = eventTime;
    }

    public long getTimeUntilEvent() {
        return timeUntilEvent;
    }

    public int getEventTime() {
        return eventTime;
    }

    public String getWeatherEvent() {
        return weatherEvent;
    }

    public CompoundNBT write() {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putString("weatherEvent", weatherEvent);
        compoundNBT.putLong("timeUntilEvent", timeUntilEvent);
        compoundNBT.putInt("eventTime", eventTime);
        return compoundNBT;
    }

    public static WeatherInstance read(CompoundNBT saveTag) {
        String weatherEvent = saveTag.getString("weatherEvent");
        long timeUntil = saveTag.getLong("timeUntilEvent");
        int eventTime = saveTag.getInt("eventTime");
        return new WeatherInstance(weatherEvent, timeUntil, eventTime);
    }
}
