package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.datastorage.BetterWeatherEventData;
import corgitaco.betterweather.helper.IsWeatherForced;
import corgitaco.betterweather.helpers.IBiomeModifier;
import corgitaco.betterweather.helpers.IBiomeUpdate;
import corgitaco.betterweather.util.WorldDynamicRegistry;
import corgitaco.betterweather.weatherevent.weatherevents.WeatherEventUtil;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;


@Mixin(ServerWorld.class)
public abstract class MixinServerWorld implements IBiomeUpdate {
    DynamicRegistries registry;

    @SuppressWarnings("ALL")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void storeUpgradablePerWorldRegistry(MinecraftServer server, Executor executor, SaveFormat.LevelSave save, IServerWorldInfo worldInfo, RegistryKey<World> key, DimensionType dimensionType, IChunkStatusListener statusListener, ChunkGenerator generator, boolean b, long seed, List<ISpecialSpawner> specialSpawners, boolean b1, CallbackInfo ci) {
        this.registry = new WorldDynamicRegistry((DynamicRegistries.Impl) server.func_244267_aX());

        WorldSettingsImport<INBT> worldSettingsImport = WorldSettingsImport.create(NBTDynamicOps.INSTANCE, server.getDataPackRegistries().getResourceManager(), (DynamicRegistries.Impl) this.registry);
        IServerConfiguration newServerConfiguration = save.readServerConfiguration(worldSettingsImport, server.getServerConfiguration().getDatapackCodec());
        Dimension dimension = newServerConfiguration.getDimensionGeneratorSettings().func_236224_e_().getOptional(key.getLocation()).get();
        ChunkGenerator dimensionChunkGenerator = dimension.getChunkGenerator();
        this.field_241102_C_/*Server Chunk Provider*/.getChunkGenerator().field_235949_c_ = dimensionChunkGenerator.getBiomeProvider();
        this.field_241102_C_/*Server Chunk Provider*/.getChunkGenerator().biomeProvider = dimensionChunkGenerator.getBiomeProvider();

        updateBiomeData();
    }

    @Override
    public void updateBiomeData() {
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : registry.getRegistry(Registry.BIOME_KEY).getEntries()) {
            Biome biome = entry.getValue();
            ((IBiomeModifier) (Object) biome).setHumidityModifier((float) uniqueTemp());
            ((IBiomeModifier) (Object) biome).setTempModifier((float) uniqueTemp());
        }
    }

    private float uniqueTemp() {
        RegistryKey<World> dimensionKey = ((ServerWorld) (Object) this).getDimensionKey();
        if (dimensionKey == World.THE_NETHER || dimensionKey == World.THE_END) {
            return -5.0F;
        } else {
            return 1.0F;
        }
    }


    @Inject(method = "func_241828_r", at = @At("HEAD"), cancellable = true)
    private void dynamicRegistryWrapper(CallbackInfoReturnable<DynamicRegistries> cir) {
        cir.setReturnValue(this.registry);
    }

    @Shadow
    public IServerWorldInfo field_241103_E_;

    @Shadow
    @Final
    private ServerChunkProvider field_241102_C_;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z", ordinal = 0))
    private boolean rollBetterWeatherEvent(GameRules gameRules, GameRules.RuleKey<GameRules.BooleanValue> key) {
        if (BetterWeatherUtil.isOverworld(((ServerWorld) (Object) this).getDimensionKey())) {
            if (gameRules.getBoolean(GameRules.DO_WEATHER_CYCLE))
                WeatherEventUtil.doWeatherAndRollWeatherEventChance(this.field_241103_E_, (ServerWorld) (Object) this);
            return false;
        } else
            return gameRules.getBoolean(GameRules.DO_WEATHER_CYCLE);
    }

    @Inject(method = "func_241113_a_", at = @At("HEAD"))
    private void setWeatherForced(int clearWeatherTime, int weatherTime, boolean rain, boolean thunder, CallbackInfo ci) {
        if (BetterWeatherUtil.isOverworld(((ServerWorld) (Object) this).getDimensionKey())) {
            ((IsWeatherForced) this.field_241103_E_).setWeatherForced(true);
            BetterWeatherEventData.get((ServerWorld) (Object) this).setWeatherForced(true);
        }
    }

    @Redirect(method = "tickEnvironment", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 1))
    private int takeAdvantageOfExistingChunkIterator(Random random, int bound, Chunk chunk, int randomTickSpeed) {
        if (BetterWeatherUtil.isOverworld(((ServerWorld) (Object) this).getDimensionKey())) {
            if (((ServerWorld) (Object) this).rand.nextInt(16) == 0)
                WeatherEventUtil.vanillaIceAndSnowChunkTicks(chunk, (ServerWorld) (Object) this);
            WeatherData.currentWeatherEvent.tickLiveChunks(chunk, (ServerWorld) (Object) this);
            return -1;
        } else
            return ((ServerWorld) (Object) this).rand.nextInt(16);
    }


    @Redirect(method = "tickEnvironment", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0))
    private int neverSpawnLightning(Random random, int bound) {
        return -1;
    }
}
