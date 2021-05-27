package corgitaco.betterweather.graphics.compiler;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ShaderCompilationException extends Exception {

    public ShaderCompilationException(String info) {
        super(info);
    }
}
