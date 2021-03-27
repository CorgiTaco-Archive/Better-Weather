package corgitaco.betterweather.mixin.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import corgitaco.betterweather.config.BetterWeatherConfig;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.World;
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
    private void readConfigsAtWorldCreation(Thread serverThread, DynamicRegistries.Impl registries, SaveFormat.LevelSave anvilConverterForAnvilFile, IServerConfiguration serverConfig, ResourcePackList dataPacks, Proxy serverProxy, DataFixer dataFixer, DataPackRegistries dataRegistries, MinecraftSessionService sessionService, GameProfileRepository profileRepo, PlayerProfileCache profileCache, IChunkStatusListenerFactory chunkStatusListenerFactory, CallbackInfo ci) {
        BetterWeatherConfig.serialize();
    }
}
