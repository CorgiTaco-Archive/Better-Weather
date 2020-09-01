package corgitaco.betterweather.mixin.common.entity;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(at = @At("HEAD"), method = "tick()V", cancellable = true)
    private void getTick(CallbackInfo ci) {
        BetterWeather.BetterWeatherEvents.entityTickEvent((LivingEntity) (Object) this);
    }

}
