package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.overlay.DebugOverlayGui;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugOverlayGui.class)
public abstract class MixinDebugOverlayGui {

    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "getDebugInfoLeft", at = @At("RETURN"))
    private void addSeasonDebugText(CallbackInfoReturnable<List<String>> cir) {
        if (this.mc.world != null) {
            SeasonContext seasonContext = ((BetterWeatherWorldData) this.mc.world).getSeasonContext();
            if (this.mc.gameSettings.showDebugInfo && seasonContext != null) {
                cir.getReturnValue().add("Season: " + WordUtils.capitalize(seasonContext.getCurrentSeason().getSeasonKey().toString().toLowerCase()) + " | " + WordUtils.capitalize(seasonContext.getCurrentSeason().getCurrentPhase().toString().replace("_", "").toLowerCase()));
                if (mc.player != null) {
                    cir.getReturnValue().add("TempOffset: " + seasonContext.getCurrentSubSeasonSettings().getTemperatureModifier(mc.world.func_241828_r().func_230521_a_(Registry.BIOME_KEY).get().getOptionalKey(mc.world.getBiome(mc.player.getPosition())).get()));
                }
            }
        }
    }
}
