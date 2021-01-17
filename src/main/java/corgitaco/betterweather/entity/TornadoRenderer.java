package corgitaco.betterweather.entity;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class TornadoRenderer extends MobRenderer<TornadoEntity, TornadoModel<TornadoEntity>> {
    private static final ResourceLocation TORNADO_TEXTURES = new ResourceLocation(BetterWeather.MOD_ID, "textures/environment/tornado.png");

    public TornadoRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new TornadoModel<>(), 1.5F);
    }

    @Override
    protected int getBlockLight(TornadoEntity entityIn, BlockPos partialTicks) {
        return 10;
    }

    @Override
    public ResourceLocation getEntityTexture(TornadoEntity entity) {
        return TORNADO_TEXTURES;
    }
}