package corgitaco.betterweather.mixin.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import corgitaco.betterweather.datastorage.SeasonSavedData;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.World;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.net.Proxy;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow
    @Nullable
    public abstract ServerWorld getWorld(RegistryKey<World> dimension);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void readConfigsAtWorldCreation(Thread serverThread, DynamicRegistries.Impl registries, SaveFormat.LevelSave anvilConverterForAnvilFile, IServerConfiguration p_i232576_4_, ResourcePackList dataPacks, Proxy serverProxy, DataFixer dataFixer, DataPackRegistries dataRegistries, MinecraftSessionService sessionService, GameProfileRepository profileRepo, PlayerProfileCache profileCache, IChunkStatusListenerFactory chunkStatusListenerFactory, CallbackInfo ci) {
//        if (!BetterWeather.useSeasons)
//            WeatherEventControllerConfig.handleConfig(BetterWeather.CONFIG_PATH.resolve(BetterWeather.MOD_ID + "-weather-controller.json"));
    }

    @Inject(method = "func_240787_a_", at = @At("TAIL"))
    private void assignWorldData(IChunkStatusListener p_240787_1_, CallbackInfo ci) {
//        BetterWeatherGeneralData.get(getWorld(World.OVERWORLD));
//        BetterWeatherEventData.get(getWorld(World.OVERWORLD));
        SeasonSavedData.get(getWorld(World.OVERWORLD));
    }
}
