package corgitaco.betterweather.mixin.server.world;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.Climate;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import corgitaco.betterweather.util.BiomeModifier;
import corgitaco.betterweather.util.BiomeUpdate;
import corgitaco.betterweather.mixin.access.ChunkManagerAccess;
import corgitaco.betterweather.mixin.access.ServerChunkProviderAccess;
import corgitaco.betterweather.mixin.access.ServerWorldAccess;
import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.util.WorldDynamicRegistry;
import corgitaco.betterweather.common.weather.WeatherContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mixin(ServerWorld.class)
public abstract class MixinServerWorld implements BiomeUpdate, BetterWeatherWorldData, Climate {

    @Shadow
    @Final
    private ServerChunkProvider chunkSource;

    private DynamicRegistries registry;

    @Nullable
    private SeasonContext seasonContext;

    @Nullable
    private WeatherContext weatherContext;

    @SuppressWarnings("ALL")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void storeUpgradablePerWorldRegistry(MinecraftServer server, Executor executor, SaveFormat.LevelSave save, IServerWorldInfo worldInfo, RegistryKey<World> key, DimensionType dimensionType, IChunkStatusListener statusListener, ChunkGenerator generator, boolean b, long seed, List<ISpecialSpawner> specialSpawners, boolean b1, CallbackInfo ci) {
        ResourceLocation worldKeyLocation = key.location();
        boolean hasPerWorldRegistry = Stream.concat(BetterWeatherConfig.WEATHER_EVENT_DIMENSIONS.stream(), BetterWeatherConfig.SEASON_DIMENSIONS.stream()).collect(Collectors.toSet()).size() > 1;

        boolean isValidWeatherEventDimension = BetterWeatherConfig.WEATHER_EVENT_DIMENSIONS.contains(worldKeyLocation.toString());
        boolean isValidSeasonDimension = BetterWeatherConfig.SEASON_DIMENSIONS.contains(worldKeyLocation.toString());

        if (hasPerWorldRegistry && (isValidWeatherEventDimension || isValidSeasonDimension)) {
            this.registry = new WorldDynamicRegistry((DynamicRegistries.Impl) server.registryAccess());
            BetterWeather.LOGGER.warn("Swapping server world gen datapack registry for \"" + key.location().toString() + "\" to a per world registry... This may have unintended side effects like mod incompatibilities in this world...");

            // Reload the world settings import with OUR implementation of the registry.
            WorldSettingsImport<INBT> worldSettingsImport = WorldSettingsImport.create(NBTDynamicOps.INSTANCE, server.getDataPackRegistries().getResourceManager(), (DynamicRegistries.Impl) this.registry);
            ChunkGenerator dimensionChunkGenerator = save.getDataTag(worldSettingsImport, server.getWorldData().getDataPackConfig()).worldGenSettings().dimensions().getOptional(worldKeyLocation).get().generator();

            // Reset the chunk generator fields in both the chunk provider and chunk manager. This is required for chunk generators to return the current biome object type required by our registry.
            // TODO: Do this earlier so mods mixing here can capture our version of the chunk generator.
            BetterWeather.LOGGER.warn("Swapping chunk generator for \"" + key.location().toString() + "\" to use the per world registry... This may have unintended side effects like mod incompatibilities in this world...");
            ((ServerChunkProviderAccess) this.chunkSource).setGenerator(dimensionChunkGenerator);
            ((ChunkManagerAccess) this.chunkSource.chunkMap).setGenerator(dimensionChunkGenerator);
            BetterWeather.LOGGER.info("Swapped the chunk generator for \"" + key.location().toString() + "\" to use the per world registry!");

            BetterWeather.LOGGER.info("Swapped world gen datapack registry for \"" + key.location().toString() + "\" to the per world registry!");
        } else {
            this.registry = server.registryAccess();
        }

        if (isValidWeatherEventDimension) {
            this.weatherContext = new WeatherContext((ServerWorld) (Object) this);
        }

        if (isValidSeasonDimension) {
            this.seasonContext = new SeasonContext((ServerWorld) (Object) this);
        }

        updateBiomeData();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void updateBiomeData() {
        List<Biome> validBiomes = this.chunkSource.getGenerator().getBiomeSource().possibleBiomes();
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : this.registry.registryOrThrow(Registry.BIOME_REGISTRY).entrySet()) {
            Biome biome = entry.getValue();
            RegistryKey<Biome> biomeKey = entry.getKey();

            if (seasonContext != null && validBiomes.contains(biome)) {
                float seasonHumidityModifier = (float) this.seasonContext.getCurrentSubSeasonSettings().getHumidityModifier(biomeKey);
                float seasonTemperatureModifier = (float) this.seasonContext.getCurrentSubSeasonSettings().getTemperatureModifier(biomeKey);
                ((BiomeModifier) (Object) biome).setSeasonTempModifier(seasonTemperatureModifier);
                ((BiomeModifier) (Object) biome).setSeasonHumidityModifier(seasonHumidityModifier);
            }

            if (weatherContext != null && validBiomes.contains(biome) && weatherContext.getCurrentEvent().isValidBiome(biome)) {
                float weatherHumidityModifier = (float) this.weatherContext.getCurrentEvent().getHumidityModifierAtPosition(null);
                float weatherTemperatureModifier = (float) this.weatherContext.getCurrentEvent().getTemperatureModifierAtPosition(null);
                ((BiomeModifier) (Object) biome).setWeatherTempModifier(weatherTemperatureModifier);
                ((BiomeModifier) (Object) biome).setWeatherHumidityModifier(weatherHumidityModifier);
            }


        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;hasSkyLight()Z"))
    private void tick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (seasonContext != null) {
            this.seasonContext.tick((ServerWorld) (Object) this);
        }
        if (weatherContext != null) {
            this.weatherContext.tick((ServerWorld) (Object) this);
        }
    }

    @Inject(method = "registryAccess", at = @At("HEAD"), cancellable = true)
    private void dynamicRegistryWrapper(CallbackInfoReturnable<DynamicRegistries> cir) {
        cir.setReturnValue(this.registry);
    }

    @ModifyConstant(method = "tick", constant = {@Constant(intValue = 168000), @Constant(intValue = 12000, ordinal = 1), @Constant(intValue = 12000, ordinal = 4)})
    private int modifyWeatherTime(int arg0) {
        SeasonContext seasonContext = this.seasonContext;
        return seasonContext != null ? (int) (arg0 * (1 / seasonContext.getCurrentSeason().getCurrentSettings().getWeatherEventChanceMultiplier())) : arg0;
    }


    @Inject(method = "setWeatherParameters", at = @At("HEAD"), cancellable = true)
    private void setWeatherForced(int clearWeatherTime, int weatherTime, boolean rain, boolean thunder, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "tickChunk", at = @At("HEAD"))
    private void tickLiveChunks(Chunk chunkIn, int randomTickSpeed, CallbackInfo ci) {
        if (weatherContext != null) {
            weatherContext.getCurrentEvent().doChunkTick(chunkIn, (ServerWorld) (Object) this);
            doLightning((ServerWorld) (Object) this, chunkIn.getPos());
        }
    }

    private void doLightning(ServerWorld world, ChunkPos chunkpos) {
        if (weatherContext == null) {
            return;
        }

        int xStart = chunkpos.getMinBlockX();
        int zStart = chunkpos.getMinBlockZ();
        WeatherEvent currentEvent = weatherContext.getCurrentEvent();
        if (currentEvent.isThundering() && world.random.nextInt(currentEvent.getLightningChance()) == 0) {
            BlockPos blockpos = ((ServerWorldAccess) world).invokeFindLightingTargetAround(world.getBlockRandomPos(xStart, 0, zStart, 15));
            Biome biome = world.getBiome(blockpos);
            if (currentEvent.isValidBiome(biome)) {
                DifficultyInstance difficultyinstance = world.getCurrentDifficultyAt(blockpos);
                boolean flag1 = world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && world.random.nextDouble() < (double) difficultyinstance.getEffectiveDifficulty() * 0.01D;
                if (flag1) {
                    SkeletonHorseEntity skeletonhorseentity = EntityType.SKELETON_HORSE.create(world);
                    skeletonhorseentity.setTrap(true);
                    skeletonhorseentity.setAge(0);
                    skeletonhorseentity.setPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                    world.addFreshEntity(skeletonhorseentity);
                }

                LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(world);
                lightningboltentity.moveTo(Vector3d.atBottomCenterOf(blockpos));
                lightningboltentity.setVisualOnly(flag1);
                world.addFreshEntity(lightningboltentity);
            }
        }
    }


    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0))
    private int neverSpawnLightning(Random random, int bound) {
        return weatherContext != null ? -1 : random.nextInt(bound);
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 1))
    private int takeAdvantageOfExistingChunkIterator(Random random, int bound, Chunk chunk, int randomTickSpeed) {
        return weatherContext != null ? -1 : ((ServerWorld) (Object) this).random.nextInt(bound);
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
    public WeatherContext getWeatherEventContext() {
        return this.weatherContext;
    }

    @Nullable
    @Override
    public WeatherContext setWeatherEventContext(WeatherContext weatherEventContext) {
        this.weatherContext = weatherEventContext;
        return this.weatherContext;
    }

    @Nullable
    @Override
    public Season getSeason() {
        return this.seasonContext.getSeason();
    }
}
