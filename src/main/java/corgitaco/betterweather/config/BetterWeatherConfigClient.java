package corgitaco.betterweather.config;

import corgitaco.betterweather.weatherevents.Blizzard;

//@Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BetterWeatherConfigClient {

//    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
//    public static COMMON_CONFIG;

    public static boolean removeSmokeParticles;

    public static boolean blizzardFog;
    public static int forcedRenderDistanceDuringBlizzards;
    public static double blizzardVolume;
    public static double blizzardPitch;
    public static double blizzardFogDensity;
    public static Blizzard.BlizzardLoopSoundTrack blizzardLoopEnumValue;


//    static {
//        COMMON_BUILDER.push("Acid_Rain_Client_Settings");
//        removeSmokeParticles = COMMON_BUILDER.comment("Remove the smoke particles emitted by the acid rain.\nDefault is false.").define("RemoveParticles", false);
//        COMMON_BUILDER.pop();
//        COMMON_BUILDER.push("Blizzard_Client_Settings");
//        forcedRenderDistanceDuringBlizzards = COMMON_BUILDER.comment("The lowered render distance that's forced when blizzards are active.\nThis is used to save performance.\nDefault 3").defineInRange("ForcedBlizzardRenderDistance", 3, 1, 16);
//        blizzardVolume = COMMON_BUILDER.comment("How loud are blizzards?\nDefault 0.5").defineInRange("BlizzardVolume", 0.4, 0.0, 10);
//        blizzardPitch = COMMON_BUILDER.comment("Blizzard Pitch\nDefault 0.5").defineInRange("BlizzardPitch", 0.4, 0.0, 10);
//        blizzardFogDensity = COMMON_BUILDER.comment("Blizzard fog Density. Higher values = denser fog.\nDefault 0.1").defineInRange("BlizzardFogDensity", 0.1, 0.0, 10);
//        blizzardLoopEnumValue = COMMON_BUILDER.comment("Blizzard Sound?\nDefault: LOOP1.").defineEnum("BlizzardSoundTrack", Blizzard.BlizzardLoopSoundTrack.LOOP2);
//        blizzardFog = COMMON_BUILDER.comment("Is there Blizzard fog?\nDefault: true").define("BlizzardFog", true);
//        COMMON_BUILDER.pop();
//        COMMON_CONFIG = COMMON_BUILDER.build();
//    }
//
//    public static void loadConfig(ForgeConfigSpec config, Path path) {
//        BetterWeather.LOGGER.info("Loading config: " + path);
//        CommentedFileConfig file = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
//        file.load();
//        config.setConfig(file);
//    }
//
//    @SubscribeEvent
//    public static void onLoad(final ModConfig.Loading configEvent) {
//
//    }
}
