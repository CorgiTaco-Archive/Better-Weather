package corgitaco.betterweather.api.weather;

import com.mojang.serialization.Codec;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.graphics.Graphics;
import corgitaco.betterweather.season.client.ColorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.biome.Biome;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class WeatherEventClientSettings {

    public static final Codec<WeatherEventClientSettings> CODEC = BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS.dispatchStable(WeatherEventClientSettings::codec, Function.identity());

    private final ColorSettings colorSettings;

    public WeatherEventClientSettings(ColorSettings colorSettings) {
        this.colorSettings = colorSettings;
    }

    public abstract boolean renderWeather(Graphics graphics, Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z, Predicate<Biome> biomePredicate);

    public abstract Codec<? extends WeatherEventClientSettings> codec();

    public abstract void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, Predicate<Biome> biomePredicate);

    public boolean sunsetSunriseColor() {
        return true;
    }

    public float skyOpacity() {
        return 1.0F;
    }

    public float dayLightDarkness() {
        return 1.0F;
    }

    public boolean drippingLeaves() {
        return false;
    }

    public float fogDensity() {
        return -1.0f;
    }

    public ColorSettings getColorSettings() {
        return colorSettings;
    }
}
