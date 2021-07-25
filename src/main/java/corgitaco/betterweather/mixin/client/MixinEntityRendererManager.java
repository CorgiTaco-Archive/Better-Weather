package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.client.entity.tornado.TornadoRenderer;
import corgitaco.betterweather.entity.BetterWeatherEntityTypes;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.resources.IReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRendererManager.class)
public abstract class MixinEntityRendererManager {

    @Shadow
    public abstract <T extends Entity> void register(EntityType<T> entityTypeIn, EntityRenderer<? super T> entityRendererIn);

    @Inject(method = "<init>", at = @At("RETURN"), cancellable = true)
    private void addBetterWeatherEntityRenderers(TextureManager textureManagerIn, ItemRenderer itemRendererIn, IReloadableResourceManager resourceManagerIn, FontRenderer fontRendererIn, GameSettings gameSettingsIn, CallbackInfo ci) {
        this.register(BetterWeatherEntityTypes.TORNADO_ENTITY_TYPE, new TornadoRenderer((EntityRendererManager) (Object) this));
    }
}
