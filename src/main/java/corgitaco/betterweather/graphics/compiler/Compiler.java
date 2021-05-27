package corgitaco.betterweather.graphics.compiler;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import static net.minecraft.util.Util.make;
import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public final class Compiler {
    private static final Queue<Integer> shaders = new LinkedList<>();

    public Compiler() {
    }

    public void compile(int type, String source) throws ShaderCompilationException {
        int shader = make(glCreateShader(type), shaders::offer);

        glShaderSource(shader, source);

        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new ShaderCompilationException(glGetShaderInfoLog(shader));
        }
    }

    // Iterate through each entry, without removing any.
    public void iterate(Consumer<Integer> consumer) {
        shaders.forEach(consumer);
    }

    // Iterate through each entry, and removes it. (FIFO)
    public void query(Consumer<Integer> consumer) {
        Integer shader;
        while ((shader = shaders.poll()) != null) {
            consumer.accept(shader);
        }
    }
}
