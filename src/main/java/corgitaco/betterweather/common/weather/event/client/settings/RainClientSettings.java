package corgitaco.betterweather.common.weather.event.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.common.weather.event.client.RainClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

public class RainClientSettings extends WeatherEventClientSettings {

    public static final Codec<RainClientSettings> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(ColorSettings.CODEC.fieldOf("colorSettings").forGetter(rainClientSettings -> {
            return rainClientSettings.getColorSettings();
        }), Codec.FLOAT.fieldOf("skyOpacity").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.skyOpacity();
        }), Codec.FLOAT.fieldOf("fogDensity").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.fogDensity();
        }), Codec.BOOL.fieldOf("sunsetSunriseColor").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.sunsetSunriseColor();
        }), ResourceLocation.CODEC.fieldOf("rainTexture").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.rainTexture;
        }), ResourceLocation.CODEC.fieldOf("snowTexture").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.snowTexture;
        })).apply(builder, RainClientSettings::new);
    });
    public final ResourceLocation rainTexture;
    public final ResourceLocation snowTexture;

    public static final Map<String, String> VALUE_COMMENTS = Util.make(new HashMap<>(WeatherEventClientSettings.VALUE_COMMENTS), (map) -> {
        map.put("rainTexture", "Texture path to the rain texture.");
        map.put("snowTexture", "Texture path to the rain texture.");
    });

    public RainClientSettings(ColorSettings colorSettings, float skyOpacity, float fogDensity, boolean sunsetSunriseColor, ResourceLocation rainTexture, ResourceLocation snowTexture) {
        super(colorSettings, skyOpacity, fogDensity, sunsetSunriseColor);
        this.rainTexture = rainTexture;
        this.snowTexture = snowTexture;
    }

    @Override
    public WeatherEventClient<?> createClientSettings() {
        return new RainClient(this);
    }

    @Override
    public Codec<? extends WeatherEventClientSettings> codec() {
        return CODEC;
    }
}
