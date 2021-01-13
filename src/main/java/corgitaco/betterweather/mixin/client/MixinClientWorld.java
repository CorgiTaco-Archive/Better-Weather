package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setupClientGameData(ClientPlayNetHandler handler, ClientWorld.ClientWorldInfo info, RegistryKey<World> key, DimensionType dimtype, int i, Supplier<IProfiler> profiler, WorldRenderer renderer, boolean b1, long b2, CallbackInfo ci) {
        BetterWeather.setWeatherData((ClientWorld)(Object) this);
        BetterWeather.setSeasonData((ClientWorld)(Object) this);
    }

    @Redirect(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    private float cancelVanillaRainSkyDarkening(ClientWorld clientWorld, float delta) {
        return 0.0F;
    }

    @Redirect(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getThunderStrength(F)F"))
    private float cancelVanillaThunderSkyDarkening(ClientWorld clientWorld, float delta) {
        return 0.0F;
    }
}
