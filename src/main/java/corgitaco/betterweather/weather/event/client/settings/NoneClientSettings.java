package corgitaco.betterweather.weather.event.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.weather.event.client.NoneClient;

public class NoneClientSettings extends WeatherEventClientSettings {
    public static final Codec<NoneClientSettings> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(ColorSettings.CODEC.fieldOf("colorSettings").forGetter(blizzardClient -> {
            return blizzardClient.getColorSettings();
        })).apply(builder, NoneClientSettings::new);
    });


    public NoneClientSettings(ColorSettings colorSettings) {
        super(colorSettings, 1.0F, -1.0F, true);
    }

    @Override
    public WeatherEventClient<?> createClientSettings() {
        return new NoneClient(this);
    }

    @Override
    public Codec<? extends WeatherEventClientSettings> codec() {
        return CODEC;
    }
}
