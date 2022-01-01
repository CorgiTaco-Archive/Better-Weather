package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.overlay.DebugOverlayGui;
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
    private Minecraft minecraft;

    @Inject(method = "getGameInformation", at = @At("RETURN"))
    private void addSeasonDebugText(CallbackInfoReturnable<List<String>> cir) {
        if (this.minecraft.level != null) {
            SeasonContext seasonContext = ((BetterWeatherWorldData) this.minecraft.level).getSeasonContext();
            if (this.minecraft.options.renderDebug && seasonContext != null) {
                cir.getReturnValue().add("Season: " + WordUtils.capitalize(seasonContext.getCurrentSeason().getSeasonKey().toString().toLowerCase()) + " | " + WordUtils.capitalize(seasonContext.getCurrentSeason().getCurrentPhase().toString().replace("_", "").toLowerCase()));
                if (minecraft.player != null) {
//                    cir.getReturnValue().add("TempOffset: " + seasonContext.getCurrentSubSeasonSettings().getTemperatureModifier(mc.world.registryAccess().registry(Registry.BIOME_KEY).get().getOptionalKey(mc.world.getBiome(mc.player.getPosition())).get()));
                }
            }
        }
    }
}
