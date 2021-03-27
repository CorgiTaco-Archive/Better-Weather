package corgitaco.betterweather.mixin.server.world;

import corgitaco.betterweather.api.Climate;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.data.storage.SeasonSavedData;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.helpers.BiomeModifier;
import corgitaco.betterweather.helpers.BiomeUpdate;
import corgitaco.betterweather.season.SeasonContext;
import corgitaco.betterweather.util.WorldDynamicRegistry;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;


@Mixin(ServerWorld.class)
public abstract class MixinServerWorld implements BiomeUpdate, BetterWeatherWorldData, Climate {

    @Shadow
    @Final
    private ServerChunkProvider serverChunkProvider;

    private DynamicRegistries registry;

    @Nullable
    private SeasonContext seasonContext;

    @SuppressWarnings("ALL")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void storeUpgradablePerWorldRegistry(MinecraftServer server, Executor executor, SaveFormat.LevelSave save, IServerWorldInfo worldInfo, RegistryKey<World> key, DimensionType dimensionType, IChunkStatusListener statusListener, ChunkGenerator generator, boolean b, long seed, List<ISpecialSpawner> specialSpawners, boolean b1, CallbackInfo ci) {
        ResourceLocation worldKeyLocation = key.getLocation();
        if (BetterWeatherConfig.SEASON_DIMENSIONS.contains(worldKeyLocation.toString()) || BetterWeatherConfig.SEASON_DIMENSIONS.contains(worldKeyLocation.getNamespace())) {
            this.registry = new WorldDynamicRegistry((DynamicRegistries.Impl) server.getDynamicRegistries());

            //Reload the world settings import with OUR implementation of the registry.
            WorldSettingsImport<INBT> worldSettingsImport = WorldSettingsImport.create(NBTDynamicOps.INSTANCE, server.getDataPackRegistries().getResourceManager(), (DynamicRegistries.Impl) this.registry);
            ChunkGenerator dimensionChunkGenerator = save.readServerConfiguration(worldSettingsImport, server.getServerConfiguration().getDatapackCodec()).getDimensionGeneratorSettings().func_236224_e_().getOptional(worldKeyLocation).get().getChunkGenerator();
            // Reset the chunk generator fields in both the chunk provider and chunk manager. This is required for chunk generators to return the current biome object type required by our registry. //TODO: Do this earlier so mods mixing here can capture our version of the chunk generator.
            this.serverChunkProvider/*Server Chunk Provider*/.generator = dimensionChunkGenerator;
            this.serverChunkProvider/*Server Chunk Provider*/.chunkManager.generator = dimensionChunkGenerator;


            this.seasonContext = new SeasonContext(SeasonSavedData.get((ServerWorld) (Object) this), key, this.registry.getRegistry(Registry.BIOME_KEY));
            updateBiomeData();
        } else {
            registry = server.getDynamicRegistries();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void updateBiomeData() {
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : registry.getRegistry(Registry.BIOME_KEY).getEntries()) {
            Biome biome = entry.getValue();
            RegistryKey<Biome> biomeKey = entry.getKey();
            float seasonHumidityModifier = seasonContext == null ? 0.0F : (float) this.seasonContext.getCurrentSubSeasonSettings().getHumidityModifier(biomeKey);
            float seasonTemperatureModifier = seasonContext == null ? 0.0F : (float) this.seasonContext.getCurrentSubSeasonSettings().getTemperatureModifier(biomeKey);

            ((BiomeModifier) (Object) biome).setHumidityModifier(seasonHumidityModifier);
            ((BiomeModifier) (Object) biome).setTempModifier(seasonTemperatureModifier);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (seasonContext != null) {
            this.seasonContext.tick((ServerWorld) (Object) this);
        }
    }

    @Inject(method = "func_241828_r", at = @At("HEAD"), cancellable = true)
    private void dynamicRegistryWrapper(CallbackInfoReturnable<DynamicRegistries> cir) {
        cir.setReturnValue(this.registry);
    }

    @ModifyConstant(method = "tick", constant = {@Constant(intValue = 168000), @Constant(intValue = 12000, ordinal = 0), @Constant(intValue = 12000, ordinal = 2)})
    private int modifyWeatherTime(int arg0) {
        SeasonContext seasonContext = this.seasonContext;
        return seasonContext != null ? (int) (arg0 * (1 / seasonContext.getCurrentSeason().getCurrentSettings().getWeatherEventChanceMultiplier())) : arg0;
    }

    @Nullable
    @Override
    public SeasonContext getSeasonContext() {
        return this.seasonContext;
    }

    @Nullable
    @Override
    public SeasonContext setSeasonContext(SeasonContext seasonContext) {
        this.seasonContext = seasonContext;
        return this.seasonContext;
    }

    @Nullable
    @Override
    public Season getSeason() {
        return this.seasonContext;
    }
}
