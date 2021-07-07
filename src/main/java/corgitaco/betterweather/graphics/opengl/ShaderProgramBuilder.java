package corgitaco.betterweather.graphics.opengl;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public final class ShaderProgramBuilder {
    private final Queue<Integer> queue = new LinkedTransferQueue<>();

    public static ShaderProgramBuilder create() {
        return new ShaderProgramBuilder();
    }

    private ShaderProgramBuilder() {
    }

    @Contract("_, _ -> this")
    public ShaderProgramBuilder compile(int type, Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            int shader = glCreateShader(type);

            glShaderSource(shader, builder.toString());

            glCompileShader(shader);
            if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
                String infoLog = glGetShaderInfoLog(shader);

                glDeleteShader(shader);

                throw new RuntimeException(infoLog);
            }

            queue.offer(shader);
        }
        return this;
    }

    @Contract("-> new")
    public ShaderProgram build() {
        return new ShaderProgram(this);
    }

    public void forEach(Consumer<Integer> consumer) {
        queue.forEach(consumer);
    }

    public void clean() {
        Integer shader;
        while ((shader = queue.poll()) != null) {
            glDeleteShader(shader);
        }
    }
}
