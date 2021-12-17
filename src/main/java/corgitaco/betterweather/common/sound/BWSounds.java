package corgitaco.betterweather.common.sound;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BWSounds {
    public static SoundEvent BLIZZARD_LOOP1 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop1"));
    public static SoundEvent BLIZZARD_LOOP2 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop2"));
    public static SoundEvent BLIZZARD_LOOP3 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop3"));
    public static SoundEvent BLIZZARD_LOOP4 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop4"));
    public static SoundEvent BLIZZARD_LOOP5 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop5"));
    public static SoundEvent BLIZZARD_LOOP6 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop6"));
    public static SoundEvent BLIZZARD_LOOP7 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop7"));

    @SubscribeEvent
    public static void bwRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                BLIZZARD_LOOP1.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop1")),
                BLIZZARD_LOOP2.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop2")),
                BLIZZARD_LOOP3.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop3")),
                BLIZZARD_LOOP4.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop4")),
                BLIZZARD_LOOP5.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop5")),
                BLIZZARD_LOOP6.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop6")),
                BLIZZARD_LOOP7.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop7"))


        );
    }
}