package corgitaco.betterweather;

import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

public class BetterWeatherUtil {
    public static int removeLeavesFromHeightMap(World world, BlockPos pos) {
        BlockPos.Mutable heightMapOriginalPos = new BlockPos.Mutable(pos.getX(), world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ());

        while(world.getBlockState(heightMapOriginalPos.down()).getBlock().isIn(BlockTags.LEAVES) || !world.getBlockState(heightMapOriginalPos.down()).getMaterial().blocksMovement() && world.getBlockState(heightMapOriginalPos.down()).getFluidState().isEmpty())
            heightMapOriginalPos.move(Direction.DOWN);

        return heightMapOriginalPos.getY();
    }
}