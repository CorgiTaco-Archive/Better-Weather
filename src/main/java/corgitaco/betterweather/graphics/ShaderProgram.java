package corgitaco.betterweather.graphics;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.graphics.compiler.Compiler;
import corgitaco.betterweather.util.function.Builder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL20;

import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public final class ShaderProgram {
    private final int program = glCreateProgram();

    private final Compiler compiler = new Compiler();
    private final Map<String, Integer> uniforms = new Object2ObjectOpenHashMap<>();

    public ShaderProgram(Builder<Compiler> builder) {
        // Attempt to compile shaders.
        try {
            builder.build(compiler);
        } catch (Exception e) { // On any error; delete the shaders, and program.
            BetterWeather.LOGGER.error("Failed to create a shader program.", e);
            delete();

            return;
        }

        // Attach all shaders, if all are compiled.
        compiler.iterate(shader -> glAttachShader(program, shader));

        glLinkProgram(program);
        status(GL_LINK_STATUS, info -> BetterWeather.LOGGER.error(info, new RuntimeException()));

        // Detach the shaders, they're no longer needed.
        compiler.iterate(shader -> glDetachShader(program, shader));

        glValidateProgram(program);
        status(GL_VALIDATE_STATUS, BetterWeather.LOGGER::warn);
    }

    private void status(int status, Consumer<String> consumer) {
        if (glGetProgrami(program, status) == GL_FALSE) {
            consumer.accept(glGetProgramInfoLog(program));
        }
    }

    public void bind() {
        glUseProgram(program);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void delete() {
        compiler.query(GL20::glDeleteShader);

        unbind();
        glDeleteProgram(program);
    }
}
