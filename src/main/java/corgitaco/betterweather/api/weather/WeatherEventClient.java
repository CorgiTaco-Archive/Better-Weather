package corgitaco.betterweather.api.weather;

import com.mojang.serialization.Codec;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.season.client.ColorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import java.util.function.Function;

public abstract class WeatherEventClient {

    public static final Codec<WeatherEventClient> CODEC = BetterWeatherRegistry.CLIENT_WEATHER_EVENT.dispatchStable(WeatherEventClient::codec, Function.identity());

    private final ColorSettings colorSettings;

    public WeatherEventClient(ColorSettings colorSettings) {
        this.colorSettings = colorSettings;
    }

    public abstract boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z);

    public abstract Codec<? extends WeatherEventClient> codec();

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
