package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import corgitaco.betterweather.season.BWSeasons;
import corgitaco.betterweather.season.Season;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.BooleanSupplier;


@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/IServerWorldInfo;setRaining(Z)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void rollWeatherEventChance(BooleanSupplier hasTimeLeft, CallbackInfo ci, IProfiler iprofiler, boolean flag, int i, int j, int k, boolean flag1, boolean flag2) {
        BetterWeather.setWeatherData(((ServerWorld) (Object) this));
        double randomDouble = ((ServerWorld) (Object) this).getRandom().nextDouble();
        double acidRainChance = Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventController().getAcidRainChance();
        double blizzardChance = Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventController().getBlizzardChance();


        if (flag1 || flag2) {
            if (randomDouble < acidRainChance)
                BetterWeather.weatherData.setEvent(BetterWeather.WeatherEvent.ACID_RAIN);
            else if (randomDouble < blizzardChance)
                BetterWeather.weatherData.setEvent(BetterWeather.WeatherEvent.BLIZZARD);
            else
                BetterWeather.weatherData.setEvent(BetterWeather.WeatherEvent.NONE);
        } else
            BetterWeather.weatherData.setEvent(BetterWeather.WeatherEvent.NONE);

        this.getPlayers().forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEvent())));
    }

//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 12000, ordinal = 0))
//    private static int thunderingRandomTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 3600))
//    private static int thunderingMinTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 168000, ordinal = 0))
//    private static int firstThunderRandomTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 12000, ordinal = 1))
//    private static int firstThunderMinTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 12000, ordinal = 2))
//    private static int rainingRandomTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 12000, ordinal = 3))
//    private static int rainingMinTime(int arg) {
//        return arg;
//    }
//
//    @ModifyConstant(method = "tick", constant = @Constant(intValue = 168000, ordinal = 1))
//    private static int firstRainRandomTime(int arg) {
//        return arg;
//    }
}
