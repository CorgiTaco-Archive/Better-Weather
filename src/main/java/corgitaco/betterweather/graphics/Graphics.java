package corgitaco.betterweather.graphics;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Graphics {

    default boolean isSupported() {
        return false;
    }
}
