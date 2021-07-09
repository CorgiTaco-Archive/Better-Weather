package corgitaco.betterweather.graphics.old;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.graphics.opengl.program.ShaderProgram;
import corgitaco.betterweather.graphics.opengl.program.ShaderProgramBuilder;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;

import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

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
        if (!(SUPPORTED = !(BetterWeather.USING_OPTIFINE || config.equalsIgnoreCase("force_off")) && GL.getCapabilities().OpenGL20)) {
            BetterWeather.LOGGER.warn("OpenGL 2.0 is not supported (disabled GLSL Shaders), or shaders are disabled in the config.");
        }
    }

    public boolean isSupported() {
        return SUPPORTED;
    }
}
