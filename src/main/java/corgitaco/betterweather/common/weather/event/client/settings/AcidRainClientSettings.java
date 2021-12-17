package corgitaco.betterweather.common.weather.event.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.common.weather.event.client.AcidRainClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

public class AcidRainClientSettings extends RainClientSettings {

    public static final Codec<AcidRainClientSettings> CODEC = RecordCodecBuilder.create((builder) -> {
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
        }), Codec.BOOL.fieldOf("smokeParticles").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.addSmokeParticles;
        })).apply(builder, AcidRainClientSettings::new);
    });

    public static final Map<String, String> VALUE_COMMENTS = Util.make(new HashMap<>(RainClientSettings.VALUE_COMMENTS), (map) -> {
        map.put("smokeParticles", "Do smoke particles appear on the ground?");
    });

    public final boolean addSmokeParticles;

    public AcidRainClientSettings(ColorSettings colorSettings, float skyOpacity, float fogDensity, boolean sunsetSunriseColor, ResourceLocation rainTexture, ResourceLocation snowTexture, boolean addSmokeParticles) {
        super(colorSettings, skyOpacity, fogDensity, sunsetSunriseColor, rainTexture, snowTexture);
        this.addSmokeParticles = addSmokeParticles;
    }

    @Override
    public WeatherEventClient<?> createClientSettings() {
        return new AcidRainClient(this);
    }

    @Override
    public Codec<? extends WeatherEventClientSettings> codec() {
        return CODEC;
    }
}
