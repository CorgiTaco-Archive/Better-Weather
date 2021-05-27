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

        if (config.equalsIgnoreCase("force_off")) {
            SUPPORTED = false;
        } else {
            if (BetterWeather.USING_OPTIFINE) {
                SUPPORTED = false;
            }

            /**
             * see {@link corgitaco.betterweather.mixin.client.MixinMainWindow}
             */
        }

        if (!SUPPORTED) {
            BetterWeather.LOGGER.info("GLSL Shaders arnt supported, or they're disabled in the config.");
        }
    }

    public boolean isSupported() {
        return false;
    }
}
