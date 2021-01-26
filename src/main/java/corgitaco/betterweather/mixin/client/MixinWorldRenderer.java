package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.helper.ViewFrustumGetter;
import corgitaco.betterweather.helper.WeatherViewFrustum;
import corgitaco.betterweather.weatherevent.weatherevents.Blizzard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer implements ViewFrustumGetter {

    @Shadow
    public int ticks;
    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private ClientWorld world;

    @Shadow
    private ViewFrustum viewFrustum;

    @Inject(at = @At("HEAD"), method = "renderRainSnow(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", cancellable = true)
    private void renderWeather(LightTexture lightmapIn, float partialTicks, double x, double y, double z, CallbackInfo ci) {
        if (BetterWeatherUtil.isOverworld((mc.world.getDimensionKey()))) {
            if (WeatherData.currentWeatherEvent.renderWeather(mc, this.world, lightmapIn, ticks, partialTicks, x, y, z))
                ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "loadRenderers()V", cancellable = true)
    private void handleOptifineCompat(CallbackInfo ci) {
        if (BetterWeather.usingOptifine && BetterWeatherUtil.isOverworld(world.getDimensionKey())) {
            if (WeatherData.currentWeatherEvent.preventChunkRendererRefreshingWhenOptifineIsPresent())
                ci.cancel();
        }
    }


    @Inject(at = @At("TAIL"), method = "loadRenderers()V", cancellable = true)
    private void forceWeatherEventRenderDistance(CallbackInfo ci) {
        if(world != null) {
            if (!BetterWeather.usingOptifine && BetterWeatherUtil.isOverworld(world.getDimensionKey())) {
                ClientPlayerEntity player = mc.player;
                if (player != null) {
                    if (Blizzard.doBlizzardsAffectDeserts(world.getBiome(mc.player.getPosition()))) {
                        int renderDistance = WeatherData.currentWeatherEvent.forcedRenderDistance();
                        if (renderDistance != mc.gameSettings.renderDistanceChunks)
                            ((WeatherViewFrustum) this.viewFrustum).forceRenderDistance(renderDistance, player.getPosX(), player.getPosY(), player.getPosZ());
                    }

                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "addRainParticles(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V", cancellable = true)
    private void stopRainParticles(ActiveRenderInfo activeRenderInfoIn, CallbackInfo ci) {
        if (mc.world != null && BetterWeatherUtil.isOverworld(world.getDimensionKey())) {
            if (WeatherData.currentWeatherEvent.weatherParticlesAndSound(activeRenderInfoIn, this.mc))
                ci.cancel();
        }
    }

    @Redirect(method = "renderSky(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    public float sunRemoval(ClientWorld clientWorld, float delta) {
        float rainStrength = this.world.getRainStrength(delta);
        return BetterWeatherUtil.isOverworld(world.getDimensionKey()) ? rainStrength * WeatherData.currentWeatherEvent.skyOpacity() : rainStrength;
    }

    @Override
    public ViewFrustum getViewFrustum() {
        return this.viewFrustum;
    }
}
