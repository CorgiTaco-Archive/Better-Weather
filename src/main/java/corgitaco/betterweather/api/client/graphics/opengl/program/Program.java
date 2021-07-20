package corgitaco.betterweather.api.client.graphics.opengl.program;

import corgitaco.betterweather.api.client.graphics.opengl.Destroyable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public abstract class Program implements Destroyable {
    protected final int program = glCreateProgram();

    @Override
    public void bind() {
        glUseProgram(program);
    }

    @Override
    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public void destroy() {
        unbind();

        glDeleteProgram(program);
    }
}
