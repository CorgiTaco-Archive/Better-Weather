package corgitaco.betterweather.mixin.access;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(World.class)
public interface WorldAccess {

    @Accessor
    void setOThunderLevel(float oThunderLevel);

    @Accessor
    void setThunderLevel(float thunderLevel);

    @Accessor
    void setORainLevel(float oRainLevel);

    @Accessor
    void setRainLevel(float rainLevel);
}
