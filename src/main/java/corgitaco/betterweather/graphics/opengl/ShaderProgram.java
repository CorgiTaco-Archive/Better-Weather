package corgitaco.betterweather.graphics.opengl;

import corgitaco.betterweather.BetterWeather;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Queue;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public final class ShaderProgram extends Program {
    private final Object2IntMap<String> uniforms = new Object2IntArrayMap<>();

    private final ShaderProgramBuilder builder;

    protected ShaderProgram(ShaderProgramBuilder builder) {
        this.builder = builder;

        Queue<Integer> queue = builder.getQueue();

        try {
            queue.forEach(shader -> glAttachShader(shader, program));

            glLinkProgram(program);
            logStatusError(GL_LINK_STATUS, info -> {
                throw new RuntimeException(info);
            });

            queue.forEach(shader -> glDetachShader(shader, program));

            glValidateProgram(program);
            logStatusError(GL_VALIDATE_STATUS, BetterWeather.LOGGER::warn);
        } catch (Exception e) {
            BetterWeather.LOGGER.error(e);

            destroy();
        }
    }

    private void logStatusError(int status, Consumer<String> consumer) {
        if (glGetProgrami(program, status) == GL_FALSE) {
            consumer.accept(glGetProgramInfoLog(program));
        }
    }

    @Override
    public void destroy() {
        Integer shader;
        while ((shader = builder.getQueue().poll()) != null) {
            glDetachShader(shader, program);

            glDeleteShader(shader);
        }

        super.destroy();
    }
}
