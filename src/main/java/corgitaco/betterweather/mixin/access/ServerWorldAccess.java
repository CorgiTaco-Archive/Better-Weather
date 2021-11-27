package corgitaco.betterweather.mixin.access;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerWorld.class)
public interface ServerWorldAccess {

    @Invoker
    BlockPos invokeFindLightingTargetAround(BlockPos pos);
}
