package corgitaco.betterweather.graphics.opengl;

import corgitaco.betterweather.BetterWeather;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public final class ShaderProgram extends Program {
    private final Object2IntMap<String> uniforms = new Object2IntArrayMap<>();

    private final ShaderProgramBuilder builder;

    protected ShaderProgram(ShaderProgramBuilder builder) {
        this.builder = builder;

        try {
            builder.forEach(shader -> glAttachShader(shader, program));

            glLinkProgram(program);
            logStatusError(GL_LINK_STATUS, info -> {
                throw new RuntimeException(info);
            });

            builder.forEach(shader -> glDetachShader(shader, program));

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

    public int getOrMapUniform(String uniform) {
        return uniforms.computeIfAbsent(uniform, key -> glGetUniformLocation(program, key));
    }

    public void uploadFloat(String uniform, float f) {
        glUniform1f(getOrMapUniform(uniform), f);
    }

    public void uploadMatrix4f(String uniform, Matrix4f matrix4f) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FloatBuffer buffer = memoryStack.mallocFloat(16);
            matrix4f.write(buffer);

            glUniformMatrix4fv(getOrMapUniform(uniform), false, buffer);
        }
    }

    @Override
    public void destroy() {
        builder.clean();

        super.destroy();
    }
}
