package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.access.IsWeatherForced;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.weatherevent.weatherevents.WeatherEventUtil;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.function.BooleanSupplier;


@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {

    @Shadow
    public IServerWorldInfo field_241103_E_;

    @Inject(method = "tick", at = @At("HEAD"))
    private void setWeatherData(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        BetterWeather.setWeatherData(((ServerWorld) (Object) this));
        if (BetterWeather.useSeasons)
            BetterWeather.setSeasonData(((ServerWorld) (Object) this));
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z", ordinal = 0))
    private boolean rollBetterWeatherEvent(GameRules gameRules, GameRules.RuleKey<GameRules.BooleanValue> key) {
        WeatherEventUtil.doWeatherAndRollWeatherEventChance(this.field_241103_E_, (ServerWorld) (Object) this);
        return false;
    }

    @Inject(method = "func_241113_a_", at = @At("HEAD"))
    private void setWeatherForced(int clearWeatherTime, int weatherTime, boolean rain, boolean thunder, CallbackInfo ci) {
        ((IsWeatherForced) this.field_241103_E_).setWeatherForced(true);
        BetterWeather.weatherData.setWeatherForced(true);
    }

    @Redirect(method = "tickEnvironment", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 1))
    private int takeAdvantageOfExistingChunkIterator(Random random, int bound, Chunk chunk, int randomTickSpeed) {
        if (((ServerWorld)(Object) this).rand.nextInt(16) == 0)
            WeatherEventUtil.vanillaIceAndSnowChunkTicks(chunk, (ServerWorld) (Object) this);
        WeatherData.currentWeatherEvent.tickLiveChunks(chunk, (ServerWorld) (Object) this);
        return -1;
    }


    @Redirect(method = "tickEnvironment", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0))
    private int neverSpawnLightning(Random random, int bound) {
        return -1;
    }


}
