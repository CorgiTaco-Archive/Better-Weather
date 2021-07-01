package corgitaco.betterweather.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.season.Season;
import net.minecraft.block.Block;
import net.minecraft.tags.ITag;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class BWSeason {

    public static final Map<String, ITag.INamedTag<Block>> ENHANCED_CROPS = new HashMap<>();
    public static final Map<String, ITag.INamedTag<Block>> UNENHANCED_CROPS = new HashMap<>();

    public static final IdentityHashMap<Season.Phase, BWSubseasonSettings> DEFAULT_SPRING_PHASES = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(Season.Phase.START, BWSubseasonSettings.DEFAULT_SPRING_START);
        map.put(Season.Phase.MID, BWSubseasonSettings.DEFAULT_SPRING_MID);
        map.put(Season.Phase.END, BWSubseasonSettings.DEFAULT_SPRING_END);
    });

    public static final IdentityHashMap<Season.Phase, BWSubseasonSettings> DEFAULT_SUMMER_PHASES = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(Season.Phase.START, BWSubseasonSettings.DEFAULT_SUMMER_START);
        map.put(Season.Phase.MID, BWSubseasonSettings.DEFAULT_SUMMER_MID);
        map.put(Season.Phase.END, BWSubseasonSettings.DEFAULT_SUMMER_END);
    });

    public static final IdentityHashMap<Season.Phase, BWSubseasonSettings> DEFAULT_AUTUMN_PHASES = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(Season.Phase.START, BWSubseasonSettings.DEFAULT_AUTUMN_START);
        map.put(Season.Phase.MID, BWSubseasonSettings.DEFAULT_AUTUMN_MID);
        map.put(Season.Phase.END, BWSubseasonSettings.DEFAULT_AUTUMN_END);
    });

    public static final IdentityHashMap<Season.Phase, BWSubseasonSettings> DEFAULT_WINTER_PHASES = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(Season.Phase.START, BWSubseasonSettings.DEFAULT_WINTER_START);
        map.put(Season.Phase.MID, BWSubseasonSettings.DEFAULT_WINTER_MID);
        map.put(Season.Phase.END, BWSubseasonSettings.DEFAULT_WINTER_END);
    });

    public static final BWSeason DEFAULT_SPRING = new BWSeason(DEFAULT_SPRING_PHASES);
    public static final BWSeason DEFAULT_SUMMER = new BWSeason(DEFAULT_SUMMER_PHASES);
    public static final BWSeason DEFAULT_AUTUMN = new BWSeason(DEFAULT_AUTUMN_PHASES);
    public static final BWSeason DEFAULT_WINTER = new BWSeason(DEFAULT_WINTER_PHASES);

    public static final Codec<BWSeason> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.simpleMap(Season.Phase.CODEC, BWSubseasonSettings.CODEC, IStringSerializable.createKeyable(Season.Phase.values())).fieldOf("phases").forGetter((seasons) -> {
            return seasons.phaseSettings;
        })).apply(builder, (p1) -> new BWSeason(new IdentityHashMap<>(p1)));
    });

    public static final Codec<BWSeason> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.simpleMap(Season.Phase.CODEC, BWSubseasonSettings.PACKET_CODEC, IStringSerializable.createKeyable(Season.Phase.values())).fieldOf("phases").forGetter((seasons) -> {
            return seasons.phaseSettings;
        })).apply(builder, (p1) -> new BWSeason(new IdentityHashMap<>(p1)));
    });

    private Season.Key key;
    private Season.Phase currentPhase;
    private final IdentityHashMap<Season.Phase, BWSubseasonSettings> phaseSettings;

    public BWSeason(IdentityHashMap<Season.Phase, BWSubseasonSettings> phaseSettings) {
        this.phaseSettings = phaseSettings;
    }

    public Season.Key getSeasonKey() {
        return key;
    }

    public void setSeasonKey(Season.Key key) {
        this.key = key;
    }

    public BWSubseasonSettings getCurrentSettings() {
        return this.phaseSettings.get(currentPhase);
    }

    public Season.Phase getCurrentPhase() {
        return currentPhase;
    }

    public BWSubseasonSettings getSettingsForPhase(Season.Phase phase) {
        return this.phaseSettings.get(phase);
    }

    public IdentityHashMap<Season.Phase, BWSubseasonSettings> getPhaseSettings() {
        return phaseSettings;
    }

    public void tick(int currentYearTime, int yearLength) {
    }

    public BWSeason setPhaseForTime(int currentYearTime, int yearLength) {
        int seasonTimeOffset = Season.getSeasonStartTime(key, yearLength);
        int seasonLocalTime = currentYearTime - seasonTimeOffset;
        int seasonLength = yearLength / Season.Key.values().length;
        int phaseLength = Season.getPhaseLength(seasonLength);

        if (seasonLocalTime < phaseLength)
            this.currentPhase = Season.Phase.START;
        else if (seasonLocalTime < (phaseLength * 2))
            this.currentPhase = Season.Phase.MID;
        else {
            this.currentPhase = Season.Phase.END;
        }
        return this;
    }
}
