package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.blockentity.BetterWeatherBlockEntityTypes;
import corgitaco.betterweather.client.tileentity.WeatherVaneBlockEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRenderDispatcher {

    @Shadow
    protected abstract <E extends TileEntity> void register(TileEntityType<E> typeIn, TileEntityRenderer<E> rendererIn);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addBetterWeatherBlockEntities(CallbackInfo ci) {
        this.register(BetterWeatherBlockEntityTypes.WEATHER_VANE_TILE_ENTITY, new WeatherVaneBlockEntityRenderer((TileEntityRendererDispatcher) (Object) this));
    }
}
