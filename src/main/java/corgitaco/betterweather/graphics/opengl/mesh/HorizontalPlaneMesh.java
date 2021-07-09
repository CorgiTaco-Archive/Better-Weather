package corgitaco.betterweather.graphics.opengl.mesh;

// debugging

import corgitaco.betterweather.BetterWeather;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public class HorizontalPlaneMesh extends Mesh {
    private static float[] VERTICES = {
            -16.5F, 0.0F, -16.5F,
             16.5F, 0.0F, -16.5F,
             16.5F, 0.0F,  16.5F,
            -16.5F, 0.0F,  16.5F
    };

    private static int[] ELEMENTS = {
            0, 1, 2,
            2, 3, 0
    };

    private final int vbo;
    private final int ebo;
    private final int count;

    public HorizontalPlaneMesh() {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FloatBuffer vBuffer = memoryStack.mallocFloat(VERTICES.length);
            vBuffer.put(VERTICES);
            vBuffer.flip();

            IntBuffer eBuffer = memoryStack.mallocInt(count = ELEMENTS.length);
            eBuffer.put(ELEMENTS);
            eBuffer.flip();

            bind();

            vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vBuffer, GL_STATIC_DRAW);

            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            ebo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, eBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

            unbind();
        }
    }

    @Override
    public void draw() {
        bind();

        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

        glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        glDisableVertexAttribArray(0);

        unbind();
    }

    @Override
    public void destroy() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        glDeleteBuffers(vbo);

        glDeleteBuffers(ebo);

        super.destroy();
    }
}
