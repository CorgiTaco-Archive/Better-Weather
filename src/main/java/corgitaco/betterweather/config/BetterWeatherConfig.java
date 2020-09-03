package corgitaco.betterweather.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = "betterweather-common")
public class BetterWeatherConfig implements ConfigData {


    @ConfigEntry.Category("acid_rain")
    @ConfigEntry.Gui.TransitiveObject
    public AcidRainConfig acid_rain = new AcidRainConfig();


    @ConfigEntry.Category("blizzard")
    @ConfigEntry.Gui.TransitiveObject
    public BlizzardConfig blizzard = new BlizzardConfig();



    //Acid Rain Config
    public static double acidRainChance = 0.2F;
    public static int tickBlockDestroySpeed = 500;
    public static boolean destroyBlocks = true;
    public static String allowedBlocksToDestroy = "GRASS,LEAVES,PLANTS,CROPS";
    public static boolean hurtEntities = true;
    public static int hurtEntityTickSpeed = 150;
    public static double hurtEntityDamage = 0.5F;
    public static String entityTypesToDamage = "MONSTER,PLAYER";
    public static String blockToChangeFromGrass = "minecraft:dirt";
    public static String blocksToNotDestroy = "";

    //Blizzard Config
    public static boolean doBlizzardsOccurInDeserts = false;
    public static boolean spawnSnowAndIce = true;
    public static boolean decaySnowAndIce = true;
    public static boolean doBlizzardsSlowPlayers = true;
    public static boolean doBlizzardsDestroyPlants = false;
    public static int tickSnowAndIcePlaceSpeed = 20;
    public static int tickSnowAndIceDecaySpeed = 50;
    public static int blizzardSlownessAmplifier = 0;
    public static double blizzardChance = 0.1;
    public static double snowDecayTemperatureThreshold = 0.15;


}
