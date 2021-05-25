package corgitaco.betterweather.api.weather;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import corgitaco.betterweather.api.season.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;

public abstract class WeatherEvent implements WeatherEventSettings {


//    public static final Codec<WeatherEvent> CODEC = RecordCodecBuilder.create((builder) -> {
//        return builder.group(Codec.of().fieldOf("currentEvent").forGetter((weatherEvent) -> {
//            return weatherEvent.codec();
//        })).apply(builder, (arg) -> new)
//    });


    private final String id;
    private final ClientWeatherEventSettings clientSettings;
    private final double defaultChance;
    private final EnumMap<Season.Key, EnumMap<Season.Phase, Double>> seasonChances;

    public WeatherEvent(String id, ClientWeatherEventSettings clientSettings, double defaultChance) {
        this(id, clientSettings, defaultChance, new EnumMap<>(Season.Key.class));
    }

    public WeatherEvent(String id, ClientWeatherEventSettings clientSettings, double defaultChance, EnumMap<Season.Key, EnumMap<Season.Phase, Double>> seasonChance) {
        this.id = id;
        this.clientSettings = clientSettings;
        this.defaultChance = defaultChance;
        this.seasonChances = seasonChance;
    }

    public final String getID() {
        return id;
    }

    public final double getDefaultChance() {
        return defaultChance;
    }

    public final EnumMap<Season.Key, EnumMap<Season.Phase, Double>> getSeasonChances() {
        return seasonChances;
    }

    public abstract void worldTick(ServerWorld world, int tickSpeed, long worldTime);

    public abstract Pair<Codec<? extends WeatherEvent>, DynamicOps<?>> config();

    public abstract Codec<? extends WeatherEvent> codec();

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
        return new TranslationTextComponent("commands.bw.setweather.success." + id.toLowerCase());
    }

    @OnlyIn(Dist.CLIENT)
    public ClientWeatherEventSettings getClientSettings() {
        return clientSettings;
    }
}