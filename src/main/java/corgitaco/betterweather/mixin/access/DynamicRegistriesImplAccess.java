package corgitaco.betterweather.mixin.access;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DynamicRegistries.Impl.class)
public interface DynamicRegistriesImplAccess {

    @Accessor
    void setKeyToSimpleRegistryMap(Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> newVal);
}
