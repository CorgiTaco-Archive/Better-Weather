package corgitaco.betterweather;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundRegistry {
    public static SoundEvent BLIZZARD = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop"));

        @SubscribeEvent
        public static void bwRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(BLIZZARD.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop")));
        }
}
