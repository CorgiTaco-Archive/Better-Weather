package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LeavesBlock.class)
public abstract class MixinLeavesBlock {

    @Inject(at = @At("HEAD"), method = "animateTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V", cancellable = true)
    private void cancelRainDrippingEffect(BlockState stateIn, World world, BlockPos pos, Random rand, CallbackInfo ci) {
        if (WeatherData.currentWeatherEvent.drippingLeaves() && (BetterWeatherUtil.isOverworld(world.getDimensionKey())))
            ci.cancel();
    }
}
