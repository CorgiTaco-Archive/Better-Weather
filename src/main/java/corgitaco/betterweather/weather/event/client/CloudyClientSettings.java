package corgitaco.betterweather.weather.event.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.graphics.Graphics;
import corgitaco.betterweather.season.client.ColorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.biome.Biome;

import java.util.function.Predicate;

public class CloudyClientSettings extends WeatherEventClientSettings {

    public static final Codec<CloudyClientSettings> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(ColorSettings.CODEC.fieldOf("colorSettings").forGetter(rainClientSettings -> {
            return rainClientSettings.getColorSettings();
        }), Codec.FLOAT.fieldOf("skyOpacity").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.skyOpacity();
        }), Codec.FLOAT.fieldOf("fogDensity").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.fogDensity();
        }), Codec.BOOL.fieldOf("sunsetSunriseColor").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.sunsetSunriseColor();
        })).apply(builder, CloudyClientSettings::new);
    });


    public CloudyClientSettings(ColorSettings colorSettings, float skyOpacity, float fogDensity, boolean sunsetSunriseColor) {
        super(colorSettings, skyOpacity, fogDensity, sunsetSunriseColor);
    }

    @Override
    public boolean renderWeather(Graphics graphics, Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z, Predicate<Biome> biomePredicate) {
        return true;
    }

    @Override
    public Codec<? extends WeatherEventClientSettings> codec() {
        return CODEC;
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, Predicate<Biome> biomePredicate) {

    }
}
