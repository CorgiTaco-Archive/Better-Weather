package corgitaco.betterweather.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import corgitaco.betterweather.BetterWeather;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BetterWeatherConfig {

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.DoubleValue acidRainChance;
    public static ForgeConfigSpec.IntValue tickBlockDestroySpeed;
    public static ForgeConfigSpec.BooleanValue destroyBlocks;
    public static ForgeConfigSpec.BooleanValue hurtEntities;
    public static ForgeConfigSpec.IntValue hurtEntityTickSpeed;
    public static ForgeConfigSpec.ConfigValue<String> entityTypesToDamage;

    static {
        COMMON_BUILDER.comment("Better Weather Settings").push("Acid_Rain_Settings").push("World_Settings");
        acidRainChance = COMMON_BUILDER.comment("The chance of acid rain that's checked every 1000 ticks.").defineInRange("Chance", 0.1, 0.0, 1.0);
        tickBlockDestroySpeed = COMMON_BUILDER.comment("How often blocks are destroyed during an acid rain event.").defineInRange("BlockDestroyTickSpeed", 500, 50, 100000);
        destroyBlocks = COMMON_BUILDER.comment("Destroy Blocks?").define("DestroyBlocks", true);
        COMMON_BUILDER.pop();
        COMMON_BUILDER.push("Entity Settings");
        hurtEntities = COMMON_BUILDER.comment("Hurt Entities?").define("HurtEntities", true);
        hurtEntityTickSpeed = COMMON_BUILDER.comment("How often are entities(including players) hurt?").defineInRange("EntityDamageTickSpeed", 150, 0, 100000);
        entityTypesToDamage = COMMON_BUILDER.comment("Allowed Values: PLAYER, MONSTER, ANIMAL.\n Default: MONSTER,PLAYER").define("EntityTypes", "MONSTER,PLAYER");
        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec config, Path path) {
        BetterWeather.LOGGER.info("Loading config: " + path);
        CommentedFileConfig file = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        config.setConfig(file);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {

    }
}
