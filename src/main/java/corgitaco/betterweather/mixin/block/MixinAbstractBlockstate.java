package corgitaco.betterweather.mixin.block;

import corgitaco.betterweather.season.BWSeasonSystem;
import corgitaco.betterweather.season.Season;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@SuppressWarnings("deprecation")
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockstate {

    @Shadow public abstract Block getBlock();

    @Shadow protected abstract BlockState getSelf();

    /**
     * This mixin exists to apply the crop growth modifier as specified by the season to the applicable crop blocks specified by the user.
     *
     * This mixin also assumes that randomTick is exclusively used for growing the given crop and not anything else.
     *
     * My attempts of modifying the age of a crop via the {@link net.minecraftforge.event.world.BlockEvent.CropGrowEvent.Pre}
     * has resulted in failure and a rather gross growing of crops(skipping an age for example going from age 1 -> 3) and property crashes(One such case w/ Atmospheric's Aloe Vera Block).
     *
     * If you are viewing this and think you might have a better solution, please submit a PR as it would be very appreciated!
     */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void cropGrowthModifier(ServerWorld world, BlockPos posIn, Random randomIn, CallbackInfo ci) {
        String blockName = Registry.BLOCK.getKey(this.getBlock()).toString();
        if (Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason).getCropToMultiplierMap().isEmpty()) {
            if (BlockTags.CROPS.contains(this.getBlock()) || BlockTags.BEE_GROWABLES.contains(this.getBlock())) {
                ci.cancel();
                cropTicker(world, posIn, randomIn, blockName, true);
            }
        } else if (Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason).getCropToMultiplierMap().containsKey(blockName)) {
            ci.cancel();
            cropTicker(world, posIn, randomIn, blockName, false);
        }
    }

    private void cropTicker(ServerWorld world, BlockPos posIn, Random randomIn, String blockName, boolean parseDouble) {
        //Collect the crop multiplier for the given subseason.
        double cropGrowthMultiplier = Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason).getCropGrowthChanceMultiplier(blockName, this.getBlock(), parseDouble);

        //Pretty self explanatory, basically run a chance on whether or not the crop will tick for this tick
        if (cropGrowthMultiplier < 1) {
            if (world.getRandom().nextDouble() < cropGrowthMultiplier) {
                this.getBlock().randomTick(this.getSelf(), world, posIn, randomIn);
            }
        }


        //Here we gather a random number of ticks that this block will tick for this given tick.
        //We do a random.nextDouble() to determine if we get the ceil or floor value for the given crop growth multiplier.
        if (cropGrowthMultiplier > 1) {
            int numberOfTicks = world.getRandom().nextInt((world.getRandom().nextDouble() + (cropGrowthMultiplier - 1) < cropGrowthMultiplier) ? (int) Math.ceil(cropGrowthMultiplier) : (int) cropGrowthMultiplier) + 1;
            for (int tick = 0; tick < numberOfTicks; tick++) {
                this.getBlock().randomTick(this.getSelf(), world, posIn, randomIn);
            }
        }
    }
}
