package corgitaco.betterweather;


import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundRegistry {
    public static SoundEvent BLIZZARD_LOOP1 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop1"));
    public static SoundEvent BLIZZARD_LOOP2 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop2"));
    public static SoundEvent BLIZZARD_LOOP3 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop3"));
    public static SoundEvent BLIZZARD_LOOP4 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop4"));
    public static SoundEvent BLIZZARD_LOOP5 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop5"));
    public static SoundEvent BLIZZARD_LOOP6 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop6"));
    public static SoundEvent BLIZZARD_LOOP7 = new SoundEvent(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_loop7"));

        public static void bwRegisterSounds() {
            registerSound(BLIZZARD_LOOP1, "blizzard_loop1");
            registerSound(BLIZZARD_LOOP2, "blizzard_loop2");
            registerSound(BLIZZARD_LOOP3, "blizzard_loop3");
            registerSound(BLIZZARD_LOOP4, "blizzard_loop4");
            registerSound(BLIZZARD_LOOP5, "blizzard_loop5");
            registerSound(BLIZZARD_LOOP6, "blizzard_loop6");
            registerSound(BLIZZARD_LOOP7, "blizzard_loop7");
        }

        public static void registerSound(SoundEvent soundEvent, String id) {
            Registry.register(Registry.SOUND_EVENT, new ResourceLocation(BetterWeather.MOD_ID, id), soundEvent);


        }
}
