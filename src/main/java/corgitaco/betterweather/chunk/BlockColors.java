package corgitaco.betterweather.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public interface BlockColors {

    Map<BlockPos, BlockState> getColoredBlocks();
}
