package corgitaco.betterweather.mixin.access;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Registry.class)
public interface RegistryAccess<T> {


    @Invoker
    Lifecycle invokeGetLifecycleByRegistry(T object);
}
