package corgitaco.betterweather.mixin.entity;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreedGoal.class)
public abstract class MixinBreedGoal {

    @Shadow
    @Final
    protected AnimalEntity animal;

    @Shadow
    @Final
    protected World level;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void seasonalBreeding(CallbackInfoReturnable<Boolean> cir) {
        SeasonContext seasonContext = ((BetterWeatherWorldData) level).getSeasonContext();
        if (seasonContext != null) {
            if (seasonContext.getCurrentSubSeasonSettings().getEntityTypeBreedingBlacklist().contains(this.animal.getType()))
                cir.setReturnValue(false);
        }
    }
}
