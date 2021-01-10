package corgitaco.betterweather.api;

import corgitaco.betterweather.season.Season;
import corgitaco.betterweather.season.SeasonSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.awt.*;

public abstract class WeatherEvent {

    private final String name;
    private final double defaultChance;

    @Nullable public SeasonSystem.SubSeasonVal currentSeason;

    public WeatherEvent(String name, double defaultChance) {
        this.name = name;
        this.defaultChance = defaultChance;
    }

    public String getName() {
        return name;
    }

    public double getDefaultChance() {
        return defaultChance;
    }

    public abstract void worldTick(ServerWorld world, int tickSpeed, long worldTime, Iterable<ChunkHolder> loadedChunks);

    public abstract void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc);

    public boolean reloadRenderers() {
        return false;
    }

    public float modifyTemperature(float biomeTemp, float modifiedBiomeTemp, double seasonModifier) {
        return modifiedBiomeTemp == Double.MAX_VALUE ? biomeTemp : modifiedBiomeTemp;
    }

    public float modifyHumidity(float biomeHumidity, float modifiedBiomeHumidity, double seasonModifier) {
        return modifiedBiomeHumidity == Double.MAX_VALUE ? biomeHumidity : modifiedBiomeHumidity;
    }

    @OnlyIn(Dist.CLIENT)
    public void weatherParticles(Minecraft minecraft, LightTexture lightTexture, double x, double y, double z, CallbackInfo ci) {
    }

    @OnlyIn(Dist.CLIENT)
    public boolean disableSkyColor() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public Color modifySkyColor(Color biomeColor, @Nullable Color modifiedColor, Color seasonColor) {
        return modifiedColor == null ? biomeColor : modifiedColor;
    }

    @OnlyIn(Dist.CLIENT)
    public Color modifyFogColor(Color biomeColor, @Nullable Color modifiedColor, Color seasonColor) {
        return modifiedColor == null ? biomeColor : modifiedColor;
    }

    public double fogDensity(Minecraft mc) {
        return Double.MAX_VALUE;
    }

    @OnlyIn(Dist.CLIENT)
    public Color modifyGrassColor(Color biomeColor, @Nullable Color modifiedColor, Color seasonColor) {
        return modifiedColor == null ? biomeColor : modifiedColor;
    }

    @OnlyIn(Dist.CLIENT)
    public Color modifyFoliageColor(Color biomeColor, @Nullable Color modifiedColor, Color seasonColor) {
        return modifiedColor == null ? biomeColor : modifiedColor;
    }
}
