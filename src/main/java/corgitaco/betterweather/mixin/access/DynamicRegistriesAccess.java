package corgitaco.betterweather.mixin.access;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DynamicRegistries.class)
public interface DynamicRegistriesAccess {

    @Accessor
    static Map<RegistryKey<? extends Registry<?>>, DynamicRegistries.CodecHolder<?>> getRegistryCodecMap() {
        throw new Error("Mixin did not apply");
    }
}
