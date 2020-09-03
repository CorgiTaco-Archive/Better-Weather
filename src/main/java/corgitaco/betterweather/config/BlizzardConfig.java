package corgitaco.betterweather.config;

import corgitaco.betterweather.weatherevents.Blizzard;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "blizzard")
public class BlizzardConfig {

    @ConfigEntry.Gui.CollapsibleObject
    public BlizzardWorld world = new BlizzardWorld();

    @ConfigEntry.Gui.CollapsibleObject
    public BlizzardClient client = new BlizzardClient();

    @ConfigEntry.Gui.CollapsibleObject
    public BlizzardEntity entity = new BlizzardEntity();

    public static class BlizzardWorld {
        @ConfigEntry.Gui.CollapsibleObject
        public SnowAndIceGeneration snow_generation = new SnowAndIceGeneration();

        @ConfigEntry.Gui.CollapsibleObject
        public SnowAndIceDecay snow_decay = new SnowAndIceDecay();

        public static class SnowAndIceGeneration {
            @ConfigEntry.Gui.PrefixText
            @Comment(value = "\nThe chance of a blizzard that's checked every 24,000 ticks(1 Minecraft day).\n" +
                    "Default: 0.1")
            @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
            public double blizzardChance = 0.1;

            @ConfigEntry.Gui.PrefixText
            @Comment(value = "\nDo blizzards occur in deserts?\n" +
                    "Default: false")
            public boolean blizzardsInDeserts = false;

            @ConfigEntry.Gui.PrefixText
            @Comment(value = "\nDo blizzards generate snow and ice?\n" +
                    "Default: true")
            public boolean spawnSnowAndIce = true;

            @ConfigEntry.Gui.PrefixText
            @Comment(value = "\nHow often(in ticks) is snow and ice placed during blizzards?\n" +
                    "Default: 15")
            @ConfigEntry.BoundedDiscrete(min = 1, max = 100000)
            public int tickSnowAndIcePlaceSpeed = 15;

            @ConfigEntry.Gui.PrefixText
            @Comment(value = "\nDo blizzards destroy plants?\n" +
                    "Default: false")
            public boolean destroyPlants = false;
        }

        public static class SnowAndIceDecay {
            @ConfigEntry.Gui.PrefixText
            @Comment(value = "\nDoes snow and ice decay after blizzards in biomes within the specified temperature threshold?\nRecommend disabling this if you have a seasons mod installed!\n" +
                    "Default: true")
            public boolean decaySnowAndIce = true;

            @ConfigEntry.Gui.PrefixText
            @Comment(value = "\nAbove what biome temperature is snow allowed to decay?\nI.E: Biome Temperature >= 0.15(Snow decays in biome temps warmer than 0.15).\n" +
                    "Default: 0.15")
            @ConfigEntry.BoundedDiscrete(min = -2, max = 2)
            public double snowDecayTemperatureThreshold = 0.15;

            @ConfigEntry.Gui.PrefixText
            @Comment(value = "\nHow often is snow and ice decayed after blizzards?\n" +
                    "Default: 50")
            @ConfigEntry.BoundedDiscrete(min = 1, max = 100000)
            public int tickSnowAndIceDecaySpeed  = 50;
        }
    }

    public static class BlizzardEntity {

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nDo Blizzard slow entities?\n" +
                "Default: true")
        public boolean doBlizzardsSlowEntities = true;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nHow slow are entities during blizzards?\n" +
                "Default: 0")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int blizzardSlownessAmplifier = 0;
    }

    public static class BlizzardClient {

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nThe lowered render distance that's forced when blizzards are active?\nThis is used to save performance.\n" +
                "Default: 3")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
        public int forcedBlizzardRenderDistance = 3;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nHow loud are blizzards?\n" +
                "Default 0.4")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
        public double blizzardVolume = 0.4;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nBlizzard Pitch\n" +
                "Default 0.4")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
        public double blizzardPitch = 0.4;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nIs there blizzard fog?\n" +
                "Default: true")
        public boolean blizzardFog = true;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nBlizzard fog Density. Higher values = denser fog.\n" +
                "Default: 0.1")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double blizzardFogDensity = 0.1;

        @ConfigEntry.Gui.PrefixText
        @Comment(value = "\nBlizzard Sound Track?\n" +
                "\nDefault: LOOP2.")
        public Blizzard.BlizzardLoopSoundTrack blizzardSoundTrack = Blizzard.BlizzardLoopSoundTrack.LOOP2;
    }
}
