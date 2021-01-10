package corgitaco.betterweather.mixin.entity;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.SeasonSystem;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreedGoal.class)
public class MixinBreedGoal {


    @Shadow @Final protected AnimalEntity animal;

    @Inject(method = "shouldExecute", at = @At("HEAD"), cancellable = true)
    private void seasonBreeding(CallbackInfoReturnable<Boolean> cir) {
        if (BetterWeather.useSeasons) {
            if (SeasonSystem.getSubSeasonFromTime(BetterWeather.seasonData.getSeasonTime(), BetterWeather.seasonData.getSeasonCycleLength()).getEntityTypeBreedingBlacklist().contains(this.animal.getType()))
                cir.setReturnValue(false);
        }
    }
}
