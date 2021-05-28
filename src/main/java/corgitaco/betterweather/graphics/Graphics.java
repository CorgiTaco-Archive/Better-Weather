package corgitaco.betterweather.graphics;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.config.BetterWeatherConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

@OnlyIn(Dist.CLIENT)
public final class Graphics {
    private static boolean SUPPORTED = false;

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
            } else {
                if (GL.getCapabilities().OpenGL20) {
                    SUPPORTED = true;
                }
            }
        }
        if (!SUPPORTED) {
            BetterWeather.LOGGER.info("GLSL Shaders arnt supported, or they're disabled in the config.");
        }
    }

    public boolean isSupported() {
        return false;
    }
}
