package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.api.client.ColorSettings;
import corgitaco.betterweather.api.client.graphics.Graphics;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.mixin.access.Vector3dAccess;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer implements Graphics {
    @Shadow
    private int ticks;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ClientWorld level;

    @Inject(at = @At("HEAD"), method = "renderSnowAndRain", cancellable = true)
    private void renderWeather(LightTexture lightmapIn, float partialTicks, double x, double y, double z, CallbackInfo ci) {
        WeatherContext weatherEventContext = ((BetterWeatherWorldData) this.level).getWeatherEventContext();
        if (weatherEventContext != null) {
            if (weatherEventContext.getCurrentEvent().getClient().renderWeather(this, minecraft, this.level, lightmapIn, ticks, partialTicks, x, y, z, weatherEventContext.getCurrentEvent()::isValidBiome)) {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "tickRain", cancellable = true)
    private void stopRainParticles(ActiveRenderInfo activeRenderInfoIn, CallbackInfo ci) {
        WeatherContext weatherEventContext = ((BetterWeatherWorldData) this.level).getWeatherEventContext();
        if (minecraft.level != null && weatherEventContext != null) {
            if (weatherEventContext.getCurrentEvent().getClient().weatherParticlesAndSound(activeRenderInfoIn, this.minecraft, this.ticks, weatherEventContext.getCurrentEvent()::isValidBiome)) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "renderSky(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainLevel(F)F"))
    public float sunRemoval(ClientWorld clientWorld, float delta) {
        float rainStrength = this.level.getRainLevel(delta);
        WeatherContext weatherEventContext = ((BetterWeatherWorldData) this.level).getWeatherEventContext();
        return weatherEventContext != null ? rainStrength * weatherEventContext.getCurrentEvent().getClient().skyOpacity(clientWorld, this.minecraft.player.blockPosition(), weatherEventContext.getCurrentEvent()::isValidBiome) : rainStrength;
    }

    @Inject(method = "buildClouds", at = @At(value = "HEAD"))
    private void modifyCloudColor(BufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vector3d cloudsColor, CallbackInfo ci) {
        WeatherContext weatherEventContext = ((BetterWeatherWorldData) this.level).getWeatherEventContext();
        if (weatherEventContext != null) {
            ColorSettings colorSettings = weatherEventContext.getCurrentEvent().getClientSettings().getColorSettings();
            double cloudColorBlendStrength = colorSettings.getCloudColorBlendStrength();
            if (cloudColorBlendStrength <= 0.0) {
                return;
            }

            int targetCloudHexColor = colorSettings.getTargetCloudHexColor();

            float r = (float) (targetCloudHexColor >> 16 & 255) / 255.0F;
            float g = (float) (targetCloudHexColor >> 8 & 255) / 255.0F;
            float b = (float) (targetCloudHexColor & 255) / 255.0F;

            float blendStrengthAtLocation = weatherEventContext.getCurrentEvent().getClient().cloudBlendStrength(this.level, new BlockPos(cloudsX, cloudsY, cloudsZ), weatherEventContext.getCurrentEvent()::isValidBiome);
            float rainStrength = this.level.getRainLevel(Minecraft.getInstance().getFrameTime());

            float blend = (float) Math.min(cloudColorBlendStrength, rainStrength * blendStrengthAtLocation);
            ((Vector3dAccess) cloudsColor).setX(MathHelper.lerp(blend, cloudsColor.x, r));
            ((Vector3dAccess) cloudsColor).setY(MathHelper.lerp(blend, cloudsColor.y, g));
            ((Vector3dAccess) cloudsColor).setZ(MathHelper.lerp(blend, cloudsColor.z, b));
        }
    }
}