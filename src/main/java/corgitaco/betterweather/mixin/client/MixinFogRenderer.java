package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

    @Redirect(method = "updateFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    private static float doNotDarkenFogWithRainStrength(ClientWorld world, float delta) {
        return ((BetterWeatherWorldData) world).getSeasonContext() != null ? 0.0F : world.getRainStrength(delta);
    }
}
