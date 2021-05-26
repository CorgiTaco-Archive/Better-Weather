package corgitaco.betterweather.api.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.season.client.ColorSettings;
import corgitaco.betterweather.weather.event.Blizzard;
import corgitaco.betterweather.weather.event.None;
import corgitaco.betterweather.weather.event.client.BlizzardClient;
import corgitaco.betterweather.weather.event.client.NoneClient;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.Set;
import java.util.function.Function;

public abstract class WeatherEvent implements WeatherEventSettings {

    public static final Codec<WeatherEvent> CODEC = BetterWeatherRegistry.WEATHER_EVENT.dispatchStable(WeatherEvent::codec, Function.identity());

    public static final Set<WeatherEvent> DEFAULT_EVENTS = Util.make(new ReferenceArraySet<>(), (set) -> {
        set.add(new Blizzard(new BlizzardClient(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0)), 0.0D));
        set.add(new None(new NoneClient(new ColorSettings(Integer.MAX_VALUE, 0.0, Integer.MAX_VALUE, 0.0)), 0.0D));
    });

    private final WeatherEventClient clientSettings;
    private final double defaultChance;
    private final EnumMap<Season.Key, EnumMap<Season.Phase, Double>> seasonChances;

    public WeatherEvent(WeatherEventClient clientSettings, double defaultChance) {
        this(clientSettings, defaultChance, new EnumMap<>(Season.Key.class));
    }

    public WeatherEvent(WeatherEventClient clientSettings, double defaultChance, EnumMap<Season.Key, EnumMap<Season.Phase, Double>> seasonChance) {
        this.clientSettings = clientSettings;
        this.defaultChance = defaultChance;
        this.seasonChances = seasonChance;
    }

    public final double getDefaultChance() {
        return defaultChance;
    }

    public final EnumMap<Season.Key, EnumMap<Season.Phase, Double>> getSeasonChances() {
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

    @OnlyIn(Dist.CLIENT)
    public WeatherEventClient getClientSettings() {
        return clientSettings;
    }
}