package corgitaco.betterweather.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.SeasonData;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;

import java.util.IdentityHashMap;

public final class Season {

    public static final IdentityHashMap<SeasonData.Phase, SubSeasonSettings> DEFAULT_SPRING_PHASES = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(SeasonData.Phase.START, SubSeasonSettings.DEFAULT_SPRING_START);
        map.put(SeasonData.Phase.MID, SubSeasonSettings.DEFAULT_SPRING_MID);
        map.put(SeasonData.Phase.END, SubSeasonSettings.DEFAULT_SPRING_END);
    });

    public static final IdentityHashMap<SeasonData.Phase, SubSeasonSettings> DEFAULT_SUMMER_PHASES = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(SeasonData.Phase.START, SubSeasonSettings.DEFAULT_SUMMER_START);
        map.put(SeasonData.Phase.MID, SubSeasonSettings.DEFAULT_SUMMER_MID);
        map.put(SeasonData.Phase.END, SubSeasonSettings.DEFAULT_SUMMER_END);
    });

    public static final IdentityHashMap<SeasonData.Phase, SubSeasonSettings> DEFAULT_AUTUMN_PHASES = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(SeasonData.Phase.START, SubSeasonSettings.DEFAULT_AUTUMN_START);
        map.put(SeasonData.Phase.MID, SubSeasonSettings.DEFAULT_AUTUMN_MID);
        map.put(SeasonData.Phase.END, SubSeasonSettings.DEFAULT_AUTUMN_END);
    });

    public static final IdentityHashMap<SeasonData.Phase, SubSeasonSettings> DEFAULT_WINTER_PHASES = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(SeasonData.Phase.START, SubSeasonSettings.DEFAULT_WINTER_START);
        map.put(SeasonData.Phase.MID, SubSeasonSettings.DEFAULT_WINTER_MID);
        map.put(SeasonData.Phase.END, SubSeasonSettings.DEFAULT_WINTER_END);
    });

    public static final Season DEFAULT_SPRING = new Season(DEFAULT_SPRING_PHASES);
    public static final Season DEFAULT_SUMMER = new Season(DEFAULT_SUMMER_PHASES);
    public static final Season DEFAULT_AUTUMN = new Season(DEFAULT_AUTUMN_PHASES);
    public static final Season DEFAULT_WINTER = new Season(DEFAULT_WINTER_PHASES);


    private SeasonData.SeasonKey seasonKey;
    private SeasonData.Phase currentPhase;
    private final IdentityHashMap<SeasonData.Phase, SubSeasonSettings> phaseSettings;


    public static final Codec<Season> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.simpleMap(SeasonData.Phase.CODEC, SubSeasonSettings.CODEC, IStringSerializable.createKeyable(SeasonData.Phase.values())).fieldOf("phases").forGetter((seasons) -> {
            return seasons.phaseSettings;
        })).apply(builder, (p1) -> new Season(new IdentityHashMap<>(p1)));
    });

    public Season(IdentityHashMap<SeasonData.Phase, SubSeasonSettings> phaseSettings) {
        this.phaseSettings = phaseSettings;
    }

    public SeasonData.SeasonKey getSeasonKey() {
        return seasonKey;
    }

    public void setSeasonKey(SeasonData.SeasonKey seasonKey) {
        this.seasonKey = seasonKey;
    }

    public SubSeasonSettings getCurrentSettings() {
        return this.phaseSettings.get(currentPhase);
    }

    public SeasonData.Phase getCurrentPhase() {
        return currentPhase;
    }

    public SubSeasonSettings getSettingsForPhase(SeasonData.Phase phase) {
        return this.phaseSettings.get(phase);
    }

    public IdentityHashMap<SeasonData.Phase, SubSeasonSettings> getPhaseSettings() {
        return phaseSettings;
    }

    public void tick(int currentSeasonTime, int seasonLength) {
        setPhaseForTime(currentSeasonTime, seasonLength);
    }

    private void setPhaseForTime(int currentSeasonTime, int seasonLength) {
        int perSeasonTime3rd = seasonLength / 3;
        int seasonOffset = seasonLength * seasonKey.ordinal();

        if (currentSeasonTime < seasonOffset + perSeasonTime3rd)
            this.currentPhase = SeasonData.Phase.START;
        else if (currentSeasonTime < seasonOffset + (perSeasonTime3rd * 2))
            this.currentPhase = SeasonData.Phase.MID;
        else {
            this.currentPhase = SeasonData.Phase.END;
        }
    }
}
