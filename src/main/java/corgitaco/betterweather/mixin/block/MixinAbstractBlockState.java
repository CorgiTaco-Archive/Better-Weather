package corgitaco.betterweather.mixin.block;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    protected abstract BlockState getSelf();

    @Inject(method = "randomTick", at = @At("RETURN"), cancellable = true)
    private void cropGrowthModifier(ServerWorld world, BlockPos posIn, Random randomIn, CallbackInfo ci) {
        SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
        if (seasonContext == null) {
            return;
        }

        BlockState state = world.getBlockState(posIn);
        // Only enhance random ticks if the block hasn't changed.
        if (state.getBlock() == this.getBlock()) {
            seasonContext.enhanceCropRandomTick(world, posIn, this.getBlock(), state, ci);
        }
    }
}
