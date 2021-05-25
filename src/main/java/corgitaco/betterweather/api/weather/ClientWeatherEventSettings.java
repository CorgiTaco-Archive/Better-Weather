package corgitaco.betterweather.api.weather;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.text.Color;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class ClientWeatherEventSettings {

    public ClientWeatherEventSettings() {
    }

    public abstract boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z);

    public boolean disableSkyColor() {
        return false;
    }

    public Color modifySkyColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return returnColor;
    }

    public Color modifyFogColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return returnColor;
    }

    public Color modifyCloudColor(Color returnColor, float rainStrength) {
        return returnColor;
    }

    public void handleFogDensity(EntityViewRenderEvent.FogDensity event, Minecraft mc) {
    }

    public Color modifyGrassColor(Color biomeColor, @Nullable Color modifiedColor, @Nullable Color seasonColor) {
        return modifiedColor == null ? biomeColor : modifiedColor;
    }

    public Color modifyFoliageColor(Color biomeColor, @Nullable Color modifiedColor, @Nullable Color seasonColor) {
        return modifiedColor == null ? biomeColor : modifiedColor;
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

    public abstract void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc);
}
