package corgitaco.betterweather.api.weatherevent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import javax.annotation.Nullable;
import java.awt.*;

public abstract class WeatherEvent {

    private final BetterWeatherID name;
    private final double defaultChance;
    private final SeasonChance seasonChance;

    @OnlyIn(Dist.CLIENT) public int postClientTicks = 20;

    public WeatherEvent(BetterWeatherID name, double defaultChance) {
        this(name, defaultChance, new SeasonChance());
    }

    public WeatherEvent(BetterWeatherID name, double defaultChance, SeasonChance seasonChance) {
        this.name = name;
        this.defaultChance = defaultChance;
        this.seasonChance = seasonChance;
    }

    public final BetterWeatherID getID() {
        return name;
    }

    public final double getDefaultChance() {
        return defaultChance;
    }

    public final SeasonChance getSeasonChance() {
        return seasonChance;
    }

    public abstract void worldTick(ServerWorld world, int tickSpeed, long worldTime, Iterable<ChunkHolder> loadedChunks);

    public abstract void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, int postClientTicksLeft);

    public boolean refreshRenderers() {
        return false;
    }

    public float modifyTemperature(float biomeTemp, float modifiedBiomeTemp, double seasonModifier) {
        return modifiedBiomeTemp == Double.MAX_VALUE ? biomeTemp : modifiedBiomeTemp;
    }

    public float modifyHumidity(float biomeHumidity, float modifiedBiomeHumidity, double seasonModifier) {
        return modifiedBiomeHumidity == Double.MAX_VALUE ? biomeHumidity : modifiedBiomeHumidity;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z);

    public void livingEntityUpdate(Entity entity) {
    }

    public boolean weatherParticlesAndSound(ActiveRenderInfo renderInfo, Minecraft mc) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean disableSkyColor() {
        return false;
    }


    @OnlyIn(Dist.CLIENT)
    public Color modifySkyColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return returnColor;
    }

    @OnlyIn(Dist.CLIENT)
    public Color modifyFogColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return returnColor;
    }

    @OnlyIn(Dist.CLIENT)
    public Color modifyCloudColor(Color returnColor, float rainStrength) {
        return returnColor;
    }

    @OnlyIn(Dist.CLIENT)
    public void handleFogDensity(EntityViewRenderEvent.FogDensity event, Minecraft mc) {
    }

    @OnlyIn(Dist.CLIENT)
    public Color modifyGrassColor(Color biomeColor, @Nullable Color modifiedColor, @Nullable Color seasonColor) {
        return modifiedColor == null ? biomeColor : modifiedColor;
    }

    @OnlyIn(Dist.CLIENT)
    public Color modifyFoliageColor(Color biomeColor, @Nullable Color modifiedColor, @Nullable Color seasonColor) {
        return modifiedColor == null ? biomeColor : modifiedColor;
    }

    @OnlyIn(Dist.CLIENT)
    public float skyOpacity() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public float daylightBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean refreshPlayerRenderer() {
        return false;
    }

    public int forcedRenderDistance() {
        return Minecraft.getInstance().gameSettings.renderDistanceChunks;
    }

    public final TranslationTextComponent successTranslationTextComponent() {
        return new TranslationTextComponent("commands.bw.setweather.success." + name.toString().toLowerCase());
    }

    public final boolean drippingLeaves() {
        return false;
    }
}
