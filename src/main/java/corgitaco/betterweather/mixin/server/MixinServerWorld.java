package corgitaco.betterweather.mixin.server;

import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWorld.class)
public class MixinServerWorld {

//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 12000, ordinal = 0))
//    private static int thunderingRandomTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 3600))
//    private static int thunderingMinTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 168000, ordinal = 0))
//    private static int firstThunderRandomTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 12000, ordinal = 1))
//    private static int firstThunderMinTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 12000, ordinal = 2))
//    private static int rainingRandomTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 12000, ordinal = 3))
//    private static int rainingMinTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 168000, ordinal = 1))
//    private static int firstRainRandomTime(int arg) {
//        return arg;
//    }
}
