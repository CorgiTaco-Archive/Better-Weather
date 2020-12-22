package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.access.IsWeatherForced;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerWorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DerivedWorldInfo.class)
public abstract class MixinDerivedWorldInfo implements IsWeatherForced {
    @Shadow @Final private IServerWorldInfo delegate;
    private boolean weatherIsForced;

    @Override
    public void setWeatherForced(boolean flag) {
    }

    @Override
    public boolean isWeatherForced() {
        return ((IsWeatherForced) this.delegate).isWeatherForced();
    }
}