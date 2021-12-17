package corgitaco.betterweather.mixin.entity;

import corgitaco.betterweather.util.BetterWeatherWorldData;
import corgitaco.betterweather.common.weather.WeatherContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {


    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void weatherLivingTickUpdate(CallbackInfo ci) {
        World world = ((Entity) (Object) this).level;
        WeatherContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
        if (weatherEventContext != null) {
            weatherEventContext.getCurrentEvent().livingEntityUpdate((LivingEntity) (Object) this);
        }
    }
}
