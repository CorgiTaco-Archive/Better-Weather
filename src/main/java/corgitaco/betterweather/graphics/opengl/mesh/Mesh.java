package corgitaco.betterweather.graphics.opengl.mesh;

import corgitaco.betterweather.graphics.opengl.Destructible;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static org.lwjgl.opengl.GL30.*;

@OnlyIn(Dist.CLIENT)
public abstract class Mesh implements Destructible {
    private final int vao = glGenVertexArrays();

    public abstract void draw();

    @Override
    public void bind() {
        glBindVertexArray(vao);
    }

    @Override
    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void destroy() {
        unbind();

        glDeleteVertexArrays(vao);
    }
}
