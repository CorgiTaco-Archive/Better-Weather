package corgitaco.betterweather.mixin.network;

import com.mojang.authlib.GameProfile;
import corgitaco.betterweather.common.network.NetworkHandler;
import corgitaco.betterweather.common.network.packet.season.SeasonContextConstructingPacket;
import corgitaco.betterweather.common.network.packet.weather.WeatherContextConstructingPacket;
import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Mutable
    @Shadow
    @Final
    private DynamicRegistries.Impl registryHolder;

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z", ordinal = 0, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void useWorldRegistry(NetworkManager netManager, ServerPlayerEntity playerIn, CallbackInfo ci, GameProfile gameprofile, PlayerProfileCache playerprofilecache, GameProfile gameprofile1, String s, CompoundNBT compoundnbt, RegistryKey registrykey, ServerWorld serverworld, ServerWorld serverworld1, String s1, IWorldInfo iworldinfo, ServerPlayNetHandler serverplaynethandler, GameRules gamerules) {
        this.registryHolder = (DynamicRegistries.Impl) serverworld1.registryAccess();
    }


    @Inject(method = "sendLevelInfo", at = @At(value = "HEAD"))
    private void sendContext(ServerPlayerEntity playerIn, ServerWorld worldIn, CallbackInfo ci) {
        SeasonContext seasonContext = ((BetterWeatherWorldData) worldIn).getSeasonContext();
        if (seasonContext != null) {
            NetworkHandler.sendToPlayer(playerIn, new SeasonContextConstructingPacket(seasonContext));
        }

        WeatherContext weatherEventContext = ((BetterWeatherWorldData) worldIn).getWeatherContext();
        if (weatherEventContext != null) {
            NetworkHandler.sendToPlayer(playerIn, new WeatherContextConstructingPacket(weatherEventContext));
        }
    }
}
