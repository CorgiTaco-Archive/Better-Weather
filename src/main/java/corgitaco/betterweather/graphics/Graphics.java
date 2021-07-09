package corgitaco.betterweather.graphics;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.concurrent.ThreadLocalRandom;

@OnlyIn(Dist.CLIENT)
public interface Graphics {

    ThreadLocalRandom getLocalRandom();

    default boolean isSupported() {
        return false;
    }
}
