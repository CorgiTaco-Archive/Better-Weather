package corgitaco.betterweather.mixin.block;

import net.minecraft.block.CropsBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CropsBlock.class)
public abstract class MixinCropBlock {



//    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/CropsBlock;getGrowthChance(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)F"))
//    private float modifyCropGrowthChance(Block blockIn, IBlockReader worldIn, BlockPos pos) {
//        return CropsBlock.getGrowthChance(blockIn, worldIn, pos);
//    }
}
