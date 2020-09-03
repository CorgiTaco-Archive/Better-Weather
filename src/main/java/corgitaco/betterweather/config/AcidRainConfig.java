package corgitaco.betterweather.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "acid_rain")
public class AcidRainConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    public AcidRainWorld world = new AcidRainWorld();

    @ConfigEntry.Gui.CollapsibleObject
    public AcidRainEntity entity = new AcidRainEntity();

    @ConfigEntry.Gui.CollapsibleObject
    public AcidRainClient client = new AcidRainClient();

    public static class AcidRainWorld {
        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nThe chance of acid rain that's checked every 24,000 ticks(1 Minecraft day).\n" +
                "Default: 0.25")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double acidRainChance = 0.25;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nHow often(in ticks) blocks are destroyed during an acid rain event.\n" +
                "Default: 0.25")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100000)
        public double blockTickDestroySpeed = 0.25;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nDestroy Blocks?\n" +
                "Default: true")
        public boolean destroyBlocks = true;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nDestroy what block materials? Allowed Values: GRASS,LEAVES,PLANTS,CROPS\n" +
                "Default: GRASS,LEAVES,PLANTS,CROPS")
        public String allowedBlocksToDestroy = "GRASS,LEAVES,PLANTS,CROPS";

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nA specific block in one of the materials you want to destroy? Blacklist them here w/ their registry names!\n" +
                "I.E: \"minecraft:rose,minecraft:wither_rose\"")
        public String blocksToNotDestroy = "";

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nThe block to change grass to.\n" +
                "Default: \"minecraft:coarse_dirt\"")
        public String blockToChangeFromGrass = "";
    }

    public static class AcidRainEntity {

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nHurt Entities?\n" +
                "Default: true")
        public boolean hurtEntities = true;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nHow often are entities(including players) hurt?\n" +
                "Default: 150")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 100000)
        public int entityDamageTickSpeed = 150;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip
        @Comment(value = "\nWhat types of entities are damaged by Acid Rain?\n" +
                "Default: MONSTER,PLAYER")
        public String entityTypesToDamage = "MONSTER,PLAYER";

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nThe amount of damage taken.\n" +
                "Default is 0.5")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 20)
        public double damageStrength = 0.5;
    }

    public static class AcidRainClient {

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nAcid Rain Smoke Particles?\n" +
                "Default: true")
        public boolean smokeParticles = true;
    }
}
