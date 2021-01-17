package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.helper.IsWeatherForced;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerWorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DerivedWorldInfo.class)
public abstract class MixinDerivedWorldInfo implements IsWeatherForced {
    @Shadow
    @Final
    private IServerWorldInfo delegate;

    @Override
    public boolean isWeatherForced() {
        return ((IsWeatherForced) this.delegate).isWeatherForced();
    }

    @Override
    public void setWeatherForced(boolean flag) {
    }
}