package corgitaco.betterweather.api.client.graphics.opengl;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Destroyable {

    void bind();

    void unbind();

    void destroy();
}
