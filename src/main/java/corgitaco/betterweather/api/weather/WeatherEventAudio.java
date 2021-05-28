package corgitaco.betterweather.api.weather;

import net.minecraft.util.SoundEvent;

public interface WeatherEventAudio {

    float getVolume();

    float getPitch();

    SoundEvent getSound();
}
