package corgitaco.betterweather.mixin;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.common.season.BWSeason;
import net.minecraft.command.Commands;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourcePack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(DataPackRegistries.class)
public abstract class MixinDataPackRegistries {

    @Inject(method = "loadResources", at = @At("HEAD"))
    private static void readConfigAndAddTags(List<IResourcePack> resourcePacks, Commands.EnvironmentType environmentType, int i, Executor executor, Executor executor1, CallbackInfoReturnable<CompletableFuture<DataPackRegistries>> cir) {
        BetterWeatherConfig.serialize();

        for (String seasonDimension : BetterWeatherConfig.SEASON_DIMENSIONS) {
            String worldKey = seasonDimension.replace(":", ".");

            for (Season.Key seasonKey : Season.Key.values()) {
                for (Season.Phase phase : Season.Phase.values()) {
                    String season = seasonKey + "-" + phase;

                    BWSeason.AFFECTED_CROPS.computeIfAbsent(season, (key) -> BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, worldKey + "_" + season.toLowerCase() + "_affected_crops")));
                    BWSeason.UNAFFECTED_CROPS.computeIfAbsent(season, (key) ->  BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, worldKey + "_" + season.toLowerCase() + "_unaffected_crops")));
                }
            }
            BWSeason.AFFECTED_CROPS.computeIfAbsent(worldKey, (key) -> BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, worldKey + "_affected_crops")));
            BWSeason.UNAFFECTED_CROPS.computeIfAbsent(worldKey, (key) -> BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, worldKey + "_unaffected_crops")));
        }
    }
}
