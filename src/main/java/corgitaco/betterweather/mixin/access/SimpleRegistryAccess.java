package corgitaco.betterweather.mixin.access;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccess<T> {

    @Accessor
    ObjectList<T> getEntryList();

    @Accessor
    Object2IntMap<T> getEntryIndexMap();

    @Accessor
    BiMap<ResourceLocation, T> getRegistryObjects();

    @Accessor
    BiMap<RegistryKey<T>, T> getKeyToObjectMap();

    @Accessor
    Map<T, Lifecycle> getObjectToLifecycleMap();
}
