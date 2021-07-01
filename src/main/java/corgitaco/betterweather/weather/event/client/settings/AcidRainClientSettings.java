package corgitaco.betterweather.weather.event.client.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.weather.event.client.AcidRainClient;
import net.minecraft.util.ResourceLocation;

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
        }), Codec.BOOL.fieldOf("snowTexture").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.addSmokeParticles;
        })).apply(builder, AcidRainClientSettings::new);
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
