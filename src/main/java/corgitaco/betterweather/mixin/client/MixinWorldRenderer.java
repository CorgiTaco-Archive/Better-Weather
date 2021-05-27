package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.graphics.Graphics;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderTypeBuffers;
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
public abstract class MixinWorldRenderer {
    private Graphics graphics;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(Minecraft mcIn, RenderTypeBuffers rainTimeBuffersIn, CallbackInfo ci) {
        graphics = new Graphics();
    }

    @Shadow
    public int ticks;
    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private ClientWorld world;

    @Inject(at = @At("HEAD"), method = "renderRainSnow(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", cancellable = true)
    private void renderWeather(LightTexture lightmapIn, float partialTicks, double x, double y, double z, CallbackInfo ci) {
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) this.world).getWeatherEventContext();
        if (weatherEventContext != null) {
            if (weatherEventContext.getCurrentEvent().getClientSettings().renderWeather(graphics, mc, this.world, lightmapIn, ticks, partialTicks, x, y, z)) {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "addRainParticles(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V", cancellable = true)
    private void stopRainParticles(ActiveRenderInfo activeRenderInfoIn, CallbackInfo ci) {
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) this.world).getWeatherEventContext();
        if (mc.world != null && weatherEventContext != null) {
            if (weatherEventContext.getCurrentEvent().weatherParticlesAndSound(activeRenderInfoIn, this.mc)) {
                ci.cancel();
            }
        }
    }

    @Redirect(method = "renderSky(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    public float sunRemoval(ClientWorld clientWorld, float delta) {
        float rainStrength = this.world.getRainStrength(delta);
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) this.world).getWeatherEventContext();
        return weatherEventContext != null ? rainStrength * weatherEventContext.getCurrentEvent().getClientSettings().skyOpacity() : rainStrength;
    }
}