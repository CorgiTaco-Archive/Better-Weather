package corgitaco.betterweather;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;


public class BetterWeatherUtil {
    public static int removeLeavesFromHeightMap(Level world, BlockPos pos) {
        BlockPos.MutableBlockPos heightMapOriginalPos = new BlockPos.MutableBlockPos(pos.getX(), world.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ());

        while (world.getBlockState(heightMapOriginalPos.below()).getBlock().is(BlockTags.LEAVES) || !world.getBlockState(heightMapOriginalPos.below()).getMaterial().blocksMotion() && world.getBlockState(heightMapOriginalPos.below()).getFluidState().isEmpty())
            heightMapOriginalPos.move(Direction.DOWN);

        return heightMapOriginalPos.getY();
    }
}