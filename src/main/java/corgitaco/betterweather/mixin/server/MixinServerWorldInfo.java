package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.access.IsWeatherForced;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWorldInfo.class)
public abstract class MixinServerWorldInfo implements IsWeatherForced {
    private boolean weatherIsForced;

    @Override
    public void setWeatherForced(boolean flag) {
        weatherIsForced = flag;
    }

    @Override
    public boolean isWeatherForced() {
        return weatherIsForced;
    }
}