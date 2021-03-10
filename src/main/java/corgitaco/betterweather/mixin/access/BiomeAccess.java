package corgitaco.betterweather.mixin.access;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.MobSpawnInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Biome.class)
public interface BiomeAccess {

    @Accessor
    Biome.Climate getClimate();

    @Accessor
    BiomeGenerationSettings getBiomeGenerationSettings();

    @Accessor
    MobSpawnInfo getMobSpawnInfo();
}
