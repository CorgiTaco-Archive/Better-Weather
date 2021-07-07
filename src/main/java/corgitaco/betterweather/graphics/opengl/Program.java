package corgitaco.betterweather.graphics.opengl;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public abstract class Program implements Destructible {
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
