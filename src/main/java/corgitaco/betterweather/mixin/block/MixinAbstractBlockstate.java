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
public abstract class MixinAbstractBlockstate {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    protected abstract BlockState getSelf();

    /**
     * This mixin exists to apply the crop growth modifier as specified by the season to the applicable crop blocks specified by the user.
     * <p>
     * This mixin also assumes that randomTick is exclusively used for growing the given crop and not anything else.
     * <p>
     * My attempts of modifying the age of a crop via the {@link net.minecraftforge.event.world.BlockEvent.CropGrowEvent.Pre}
     * has resulted in failure and a rather gross growing of crops(skipping an age for example going from age 1 -> 3) and property crashes(One such case w/ Atmospheric's Aloe Vera Block).
     * <p>
     * If you are viewing this and think you might have a better solution, please submit a PR as it would be very appreciated!
     */
    @Inject(method = "randomTick", at = @At("RETURN"), cancellable = true)
    private void cropGrowthModifier(ServerWorld world, BlockPos posIn, Random randomIn, CallbackInfo ci) {
        SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
        if (seasonContext != null)
            seasonContext.tickCrops(world, posIn, this.getBlock(), this.getSelf(), ci);
    }
}
