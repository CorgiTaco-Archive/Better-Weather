package corgitaco.betterweather.entity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class TornadoRenderer extends MobRenderer<TornadoEntity, TornadoModel<TornadoEntity>> {
    private static final ResourceLocation TORNADO_TEXTURES = new ResourceLocation("textures/entity/blaze.png");

    public TornadoRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new TornadoModel<>(), 1.5F);
    }

    @Override
    public ResourceLocation getEntityTexture(TornadoEntity entity) {
        return TORNADO_TEXTURES;
    }
}