package corgitaco.betterweather.mixin.server.commands;

import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.LocateBiomeCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocateBiomeCommand.class)
public abstract class MixinLocateBiomeCommand {

    @Redirect(method = "locateBiome", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;registryAccess()Lnet/minecraft/util/registry/DynamicRegistries;"))
    private static DynamicRegistries useWorldDynamicRegistries(MinecraftServer minecraftServer, CommandSource source, ResourceLocation biomeID) {
        return source.getLevel().registryAccess();
    }
}
