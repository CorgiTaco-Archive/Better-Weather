//package corgitaco.betterweather.mixin.block;
//
//import corgitaco.betterweather.season.BWSeasonSystem;
//import corgitaco.betterweather.season.Season;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.CropsBlock;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockReader;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin(CropsBlock.class)
//public abstract class MixinCropBlock {
//
//    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/CropsBlock;getGrowthChance(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)F"))
//    private float modifyCropGrowthChance(Block blockIn, IBlockReader worldIn, BlockPos pos) {
//        return (float) (getGrowthChanceBW(blockIn, worldIn, pos) * Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason).getCropGrowthChanceMultiplier());
//    }
//
//    private static float getGrowthChanceBW(Block blockIn, IBlockReader worldIn, BlockPos pos) {
//        float f = 1.0F;
//        BlockPos blockpos = pos.down();
//
//        for(int i = -1; i <= 1; ++i) {
//            for(int j = -1; j <= 1; ++j) {
//                float f1 = 0.0F;
//                BlockState blockstate = worldIn.getBlockState(blockpos.add(i, 0, j));
//                if (blockstate.canSustainPlant(worldIn, blockpos.add(i, 0, j), net.minecraft.util.Direction.UP, (net.minecraftforge.common.IPlantable) blockIn)) {
//                    f1 = 1.0F;
//                    if (blockstate.isFertile(worldIn, pos.add(i, 0, j))) {
//                        f1 = 3.0F;
//                    }
//                }
//
//                if (i != 0 || j != 0) {
//                    f1 /= 4.0F;
//                }
//
//                f += f1;
//            }
//        }
//
//        BlockPos blockpos1 = pos.north();
//        BlockPos blockpos2 = pos.south();
//        BlockPos blockpos3 = pos.west();
//        BlockPos blockpos4 = pos.east();
//        boolean flag = blockIn == worldIn.getBlockState(blockpos3).getBlock() || blockIn == worldIn.getBlockState(blockpos4).getBlock();
//        boolean flag1 = blockIn == worldIn.getBlockState(blockpos1).getBlock() || blockIn == worldIn.getBlockState(blockpos2).getBlock();
//        if (flag && flag1) {
//            f /= 2.0F;
//        } else {
//            boolean flag2 = blockIn == worldIn.getBlockState(blockpos3.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.north()).getBlock() || blockIn == worldIn.getBlockState(blockpos4.south()).getBlock() || blockIn == worldIn.getBlockState(blockpos3.south()).getBlock();
//            if (flag2) {
//                f /= 2.0F;
//            }
//        }
//
//        return f;
//    }
//}
