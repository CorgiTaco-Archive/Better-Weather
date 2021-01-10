package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.access.IsWeatherForced;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import corgitaco.betterweather.season.SeasonSystem;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;


@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {


    @Shadow
    public IServerWorldInfo field_241103_E_;


    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();

    @Inject(method = "tick", at = @At("HEAD"))
    private void setWeatherData(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        BetterWeather.setWeatherData(((ServerWorld) (Object) this));
        if (BetterWeather.useSeasons)
            BetterWeather.setSeasonData(((ServerWorld) (Object) this));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/IServerWorldInfo;setRaining(Z)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void rollBetterWeatherEvent(BooleanSupplier hasTimeLeft, CallbackInfo ci, IProfiler iprofiler, boolean flag, int i, int j, int k, boolean flag1, boolean flag2) {
        Random random = ((ServerWorld) (Object) this).getRandom();
        if (BetterWeather.useSeasons)
            SeasonSystem.rollWeatherEventChanceForSeason(random, this.field_241103_E_.isRaining(), this.field_241103_E_.isThundering(), (ServerWorldInfo) this.field_241103_E_, this.getPlayers());
        else
            WeatherEventSystem.rollWeatherEventChance(random, this.field_241103_E_.isRaining(), this.field_241103_E_.isThundering(), (ServerWorldInfo) this.field_241103_E_, this.getPlayers());

    }

    @Inject(method = "func_241113_a_", at = @At("HEAD"))
    private void setWeatherForced(int clearWeatherTime, int weatherTime, boolean rain, boolean thunder, CallbackInfo ci) {
        ((IsWeatherForced) this.field_241103_E_).setWeatherForced(true);
        BetterWeather.weatherData.setWeatherForced(true);
    }
}
