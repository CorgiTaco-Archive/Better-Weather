package corgitaco.betterweather.mixin;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class MixinWorld implements BetterWeatherWorldData {
}
