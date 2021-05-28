package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.client.audio.WeatherSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.IAmbientSoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stats.StatisticsManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Shadow
    @Final
    private List<IAmbientSoundHandler> ambientSoundHandlers;

    @Inject(method = "<init>", at = @At("RETURN"), cancellable = true)
    private void weatherAmbience(Minecraft mc, ClientWorld world, ClientPlayNetHandler connection, StatisticsManager stats, ClientRecipeBook recipeBook, boolean clientSneakState, boolean clientSprintState, CallbackInfo ci) {
        this.ambientSoundHandlers.add(new WeatherSoundHandler((ClientPlayerEntity) (Object) this, mc.getSoundHandler(), world.getBiomeManager()));
    }
}
