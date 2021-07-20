package corgitaco.betterweather.mixin.client.chunk;

import corgitaco.betterweather.chunk.BlockColors;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(Chunk.class)
public class MixinChunk implements BlockColors {

    private final Map<BlockPos, BlockState> coloredBlocks = new Object2ObjectArrayMap<>();

    @Override
    public Map<BlockPos, BlockState> getColoredBlocks() {
        return this.coloredBlocks;
    }
}
