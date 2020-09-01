package corgitaco.betterweather.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.config.BetterWeatherConfigClient;
import corgitaco.betterweather.weatherevents.Blizzard;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    private static int idx2 = 0;

    @Inject(at = @At("HEAD"), method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZ)V", cancellable = true)
    private static void blizzardFogDensity(Camera camera, FogRenderer.FogMode fogMode, float f, boolean bl, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (BetterWeatherConfigClient.blizzardFog) {
            if (minecraft.level != null && minecraft.player != null) {
                BlockPos playerPos = new BlockPos(minecraft.player.position());
                if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard() && minecraft.level.getLevelData().isRaining() && Blizzard.doBlizzardsAffectDeserts(minecraft.level.getBiome(playerPos))) {
                    RenderSystem.fogDensity(0.1F);
                    if (idx2 != 0)
                        idx2 = 0;
                } else {
                    if (idx2 == 0) {
                        idx2++;
                    }
                }
            }
        }
    }
}
