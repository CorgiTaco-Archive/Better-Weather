package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LeavesBlock.class)
public class MixinLeavesBlock {


    @Inject(at = @At("HEAD"), method = "animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V", cancellable = true)
    private void cancelRainDrippingEffect(BlockState blockState, Level level, BlockPos blockPos, Random random, CallbackInfo ci) {
        if (level.getLevelData().isRaining() && BetterWeather.BetterWeatherEvents.weatherData.isBlizzard())
            ci.cancel();
    }
}
