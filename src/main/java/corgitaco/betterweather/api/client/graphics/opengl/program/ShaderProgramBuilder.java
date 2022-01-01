package corgitaco.betterweather.api.client.graphics.opengl.program;

import net.minecraft.resources.IResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    public ShaderProgramBuilder compile(int type, IResource resource) throws IOException {
        try (InputStream stream = resource.getInputStream()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8.newDecoder()))) {
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
                } else {
                    queue.offer(shader);
                }
            }
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
