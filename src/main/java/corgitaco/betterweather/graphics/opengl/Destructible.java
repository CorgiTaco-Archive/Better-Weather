package corgitaco.betterweather.graphics.opengl;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Destructible {

    void bind();

    void unbind();

    void destroy();
}
