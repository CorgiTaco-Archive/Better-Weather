package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.access.IsWeatherForced;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import corgitaco.betterweather.season.BWSeasons;
import corgitaco.betterweather.season.Season;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
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

    @Shadow
    public IServerWorldInfo field_241103_E_;

    private static int tickCounter = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void setWeatherData(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        BetterWeather.setWeatherData(((ServerWorld) (Object) this));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/IServerWorldInfo;setRaining(Z)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void rollWeatherEventChance(BooleanSupplier hasTimeLeft, CallbackInfo ci, IProfiler iprofiler, boolean flag, int i, int j, int k, boolean flag1, boolean flag2) {
        double randomDouble = ((ServerWorld) (Object) this).getRandom().nextDouble();
        double acidRainChance = Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventController().getAcidRainChance();
        double blizzardChance = Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventController().getBlizzardChance();

        boolean isRainActive = this.field_241103_E_.isRaining() || this.field_241103_E_.isThundering();

        if (!isRainActive) {
            if (!BetterWeather.weatherData.isModified()) {
                this.field_241103_E_.setRainTime((int) (this.field_241103_E_.getRainTime() * (1 / Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventChanceMultiplier())));
                this.field_241103_E_.setThunderTime((int) (this.field_241103_E_.getThunderTime() * (1 / Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventChanceMultiplier())));
                BetterWeather.weatherData.setModified(true);
            }
        }
        else {
            if (BetterWeather.weatherData.isModified())
                BetterWeather.weatherData.setModified(false);
        }


        if (tickCounter == 0) {
            if (isRainActive && !BetterWeather.weatherData.isWeatherForced()) {
                if (randomDouble < acidRainChance)
                    BetterWeather.weatherData.setEvent(BetterWeather.WeatherEvent.ACID_RAIN);
                else if (randomDouble < blizzardChance)
                    BetterWeather.weatherData.setEvent(BetterWeather.WeatherEvent.BLIZZARD);
                else
                    BetterWeather.weatherData.setEvent(BetterWeather.WeatherEvent.NONE);
                tickCounter++;
                this.getPlayers().forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEvent(), BetterWeather.weatherData.isWeatherForced(), BetterWeather.weatherData.isModified())));
            }
        } else {
            if (!isRainActive) {
                if (tickCounter > 0) {
                    BetterWeather.weatherData.setEvent(BetterWeather.WeatherEvent.NONE);
                    ((IsWeatherForced) this.field_241103_E_).setWeatherForced(false);
                    BetterWeather.weatherData.setWeatherForced(((IsWeatherForced) this.field_241103_E_).isWeatherForced());
                    this.getPlayers().forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEvent(), BetterWeather.weatherData.isWeatherForced(), BetterWeather.weatherData.isModified())));
                    tickCounter = 0;
                }
            }
        }
    }

    @Inject(method = "func_241113_a_", at = @At("HEAD"))
    private void setWeatherForced(int clearWeatherTime, int weatherTime, boolean rain, boolean thunder, CallbackInfo ci) {
        ((IsWeatherForced) this.field_241103_E_).setWeatherForced(true);
        BetterWeather.weatherData.setWeatherForced(true);
    }
}
