package corgitaco.betterweather.graphics.old;

import com.google.common.collect.ImmutableMap;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.graphics.opengl.ShaderProgram;
import corgitaco.betterweather.graphics.opengl.ShaderProgramBuilder;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

@OnlyIn(Dist.CLIENT)
public final class Graphics {
    private static boolean SUPPORTED = false;

    @SuppressWarnings("unused")
    private void exampleUsage() {
        ShaderProgramBuilder builder = ShaderProgramBuilder.create();

        try {
            builder
                    .compile(GL_FRAGMENT_SHADER, Paths.get("fragment path"))
                    .compile(GL_VERTEX_SHADER, Paths.get("vertex path"));
        } catch (Exception e) {
            BetterWeather.LOGGER.error(e);

            builder.clean();
        }

        ShaderProgram program = builder.build();

        Matrix4f matrix4f = new Matrix4f();

        // in render loop
        program.bind();

        matrix4f.setIdentity();
        matrix4f.translate(new Vector3f(0.0F, 1.0F, 0.0F));

        // render mesh

        program.uploadMatrix4f("matrix4f", matrix4f);

        program.unbind();

        // program.destroy(); when the client closes
    }

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
