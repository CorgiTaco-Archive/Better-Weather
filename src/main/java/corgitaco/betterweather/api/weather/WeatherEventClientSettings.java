package corgitaco.betterweather.api.weather;

import com.mojang.serialization.Codec;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.graphics.Graphics;
import corgitaco.betterweather.season.client.ColorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.EntityViewRenderEvent;

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

    public abstract void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc);


    public boolean disableSkyColor() {
        return false;
    }

    public void handleFogDensity(EntityViewRenderEvent.FogDensity event, Minecraft mc) {
    }

    public float skyOpacity() {
        return 1.0F;
    }

    public float dayLightDarkness() {
        return 1.0F;
    }

    public boolean refreshPlayerRenderer() {
        return false;
    }

    public int forcedRenderDistance() {
        return Minecraft.getInstance().gameSettings.renderDistanceChunks;
    }

    public boolean preventChunkRendererRefreshingWhenOptifineIsPresent() {
        return false;
    }

    public boolean drippingLeaves() {
        return false;
    }

    public void onCommandWeatherChange() {
    }

    public ColorSettings getColorSettings() {
        return colorSettings;
    }
}
