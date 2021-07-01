package corgitaco.betterweather.mixin;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.season.BWSeason;
import net.minecraft.block.Block;
import net.minecraft.command.Commands;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourcePack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(DataPackRegistries.class)
public class MixinDatapackRegistries {

    @Inject(method = "func_240961_a_", at = @At("HEAD"))
    private static void readConfigAndAddTags(List<IResourcePack> resourcePacks, Commands.EnvironmentType environmentType, int i, Executor executor, Executor executor1, CallbackInfoReturnable<CompletableFuture<DataPackRegistries>> cir) {
        BetterWeatherConfig.serialize();
        Map<String, ITag.INamedTag<Block>> enhancedCrops = new HashMap<>();
        Map<String, ITag.INamedTag<Block>> unenhancedCrops = new HashMap<>();

        for (String seasonDimension : BetterWeatherConfig.SEASON_DIMENSIONS) {
            for (Season.Key seasonKey : Season.Key.values()) {
                for (Season.Phase phase : Season.Phase.values()) {
                    String key = seasonKey + "-" + phase;

                    enhancedCrops.put(key, BWSeason.ENHANCED_CROPS.containsKey(key) ? BWSeason.ENHANCED_CROPS.get(key) : BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, seasonDimension.replace(":", ".") + "_" + key.toLowerCase() + "_enhanced_crops")));
                    unenhancedCrops.put(key, BWSeason.UNENHANCED_CROPS.containsKey(key) ? BWSeason.ENHANCED_CROPS.get(key) : BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, seasonDimension.replace(":", ".") + "_" + key.toLowerCase() + "_unenhanced_crops")));
                }
            }

            BWSeason.ENHANCED_CROPS.clear();
            BWSeason.UNENHANCED_CROPS.clear();

            BWSeason.ENHANCED_CROPS.putAll(enhancedCrops);
            BWSeason.UNENHANCED_CROPS.putAll(unenhancedCrops);
        }
    }
}
