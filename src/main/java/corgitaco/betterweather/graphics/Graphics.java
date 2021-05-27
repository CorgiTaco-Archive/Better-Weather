package corgitaco.betterweather.graphics;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.config.BetterWeatherConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class Graphics {
    private static boolean SUPPORTED = true;

    // Created when the world renderer is created.
    public Graphics() {
        @Nullable
        String config = BetterWeatherConfig.COMPATIBILITY_MODE;
        if (config == null) {
            config = "auto";
        }

        if (config.equalsIgnoreCase("force_legacy")) {
            SUPPORTED = false;
        } else {
            if (BetterWeather.USING_OPTIFINE) {
                SUPPORTED = false;
            }

            //todo: mixin into the window class, and test for opengl version
        }

        if (!SUPPORTED) {
            BetterWeather.LOGGER.info("OpenGL 3.0+ isn't supported, or is disabled.");
        }
    }

    public boolean isSupported() {
        return SUPPORTED;
    }
}
