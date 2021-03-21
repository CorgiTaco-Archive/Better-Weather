package corgitaco.betterweather.mixin;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class MixinWorld implements BetterWeatherWorldData {

//    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
//    private void removeThunderStrength(float delta, CallbackInfoReturnable<Float> cir) {
//        if (this.getSeasonContext() != null)
//            cir.setReturnValue(0.0F);
//    }
//
//    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
//    private void markIsThunderingIfThunderingEvent(CallbackInfoReturnable<Boolean> cir) {
//        if (this.getSeasonContext() != null) {
//            cir.setReturnValue(BetterWeatherEventData.get((World) (Object) this).getEventID().equals(WeatherEventSystem.DEFAULT_THUNDER));
//        }
//    }
}
