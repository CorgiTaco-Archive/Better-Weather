package corgitaco.betterweather.mixin;

import corgitaco.betterweather.BetterWeatherUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow public abstract RegistryKey<World> getDimensionKey();

    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    private void removeThunderStrength(float delta, CallbackInfoReturnable<Float> cir) {
        if (BetterWeatherUtil.isOverworld(this.getDimensionKey()))
            cir.setReturnValue(0.0F);
    }
}
