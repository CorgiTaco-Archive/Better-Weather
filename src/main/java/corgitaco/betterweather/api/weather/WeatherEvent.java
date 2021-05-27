package corgitaco.betterweather.api.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.season.client.ColorSettings;
import corgitaco.betterweather.weather.event.Blizzard;
import corgitaco.betterweather.weather.event.None;
import corgitaco.betterweather.weather.event.client.BlizzardClientSettings;
import corgitaco.betterweather.weather.event.client.NoneClientSettings;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class WeatherEvent implements WeatherEventSettings {

    public static final Codec<WeatherEvent> CODEC = BetterWeatherRegistry.WEATHER_EVENT.dispatchStable(WeatherEvent::codec, Function.identity());

    public static final Map<Season.Key, Map<Season.Phase, Double>> DEFAULT = Util.make(new IdentityHashMap<>(), (map) -> {
        for (Season.Key value : Season.Key.values()) {
            IdentityHashMap<Season.Phase, Double> phaseDoubleMap = new IdentityHashMap<>();
            for (Season.Phase phase : Season.Phase.values()) {
                phaseDoubleMap.put(phase, 0.0D);
            }
            map.put(value, phaseDoubleMap);
        }
    });

    public static final Map<String, String> VALUE_COMMENTS = Util.make(new HashMap<>(ColorSettings.VALUE_COMMENTS), (map) -> {
        map.put("defaultChance", "What is the default chance for this weather event to occur? This value is only used when Seasons are NOT present in the given dimension.");
        map.put("type", "Target to configure settings in this config.");
        map.put("seasonChances", "What is the chance for this weather event to occur for the given season (phase)?");
    });

    public static final None NONE = new None(new NoneClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0)), DEFAULT);
    public static final Blizzard BLIZZARD = new Blizzard(new BlizzardClientSettings(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0), false, new ResourceLocation("minecraft:textures/environment/snow.png")), 0.0D, DEFAULT);

    public static final Set<WeatherEvent> DEFAULT_EVENTS = Util.make(new ReferenceArraySet<>(), (set) -> {
        set.add(BLIZZARD);
        set.add(NONE);
    });

    private final WeatherEventClientSettings clientSettings;
    private final double defaultChance;
    private final Map<Season.Key, Map<Season.Phase, Double>> seasonChances;
    private String name;

    public WeatherEvent(WeatherEventClientSettings clientSettings, double defaultChance, Map<Season.Key, Map<Season.Phase, Double>> seasonChance) {
        this.clientSettings = clientSettings;
        this.defaultChance = defaultChance;
        this.seasonChances = seasonChance;
    }

    public final double getDefaultChance() {
        return defaultChance;
    }

    public final Map<Season.Key, Map<Season.Phase, Double>> getSeasonChances() {
        return seasonChances;
    }

    public abstract void worldTick(ServerWorld world, int tickSpeed, long worldTime);

    public abstract Codec<? extends WeatherEvent> codec();

    public abstract DynamicOps<?> configOps();

    public float modifyTemperature(float biomeTemp, float modifiedBiomeTemp) {
        return modifiedBiomeTemp == Double.MAX_VALUE ? biomeTemp : modifiedBiomeTemp;
    }

    public float modifyHumidity(float biomeHumidity, float modifiedBiomeHumidity) {
        return modifiedBiomeHumidity == Double.MAX_VALUE ? biomeHumidity : modifiedBiomeHumidity;
    }

    public void livingEntityUpdate(Entity entity) {
    }

    public boolean weatherParticlesAndSound(ActiveRenderInfo renderInfo, Minecraft mc) {
        return true;
    }

    /**
     * This is called in the chunk ticking iterator.
     */
    public void tickLiveChunks(Chunk chunk, ServerWorld world) {
    }

    public boolean fillBlocksWithWater() {
        return false;
    }

    public boolean spawnSnowInFreezingClimates() {
        return false;
    }

    public final TranslationTextComponent successTranslationTextComponent() {
        return new TranslationTextComponent("commands.bw.setweather.success." + BetterWeatherRegistry.WEATHER_EVENT.getKey(codec()).toString());
    }

    public WeatherEvent setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @OnlyIn(Dist.CLIENT)
    public WeatherEventClientSettings getClientSettings() {
        return clientSettings;
    }
}