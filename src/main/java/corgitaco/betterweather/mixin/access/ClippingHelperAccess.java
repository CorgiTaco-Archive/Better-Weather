package corgitaco.betterweather.mixin.access;

import net.minecraft.client.renderer.culling.ClippingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClippingHelper.class)
public interface ClippingHelperAccess {

    @Accessor
    double getCameraX();

    @Accessor
    double getCameraY();

    @Accessor
    double getCameraZ();

}
