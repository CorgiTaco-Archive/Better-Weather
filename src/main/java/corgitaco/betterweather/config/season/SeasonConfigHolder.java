package corgitaco.betterweather.config.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.season.Season;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;

import java.util.IdentityHashMap;

public class SeasonConfigHolder {
    private final int seasonCycleLength;
    private final IdentityHashMap<SeasonData.SeasonKey, Season> seasonKeySeasonMap;


    public static final IdentityHashMap<SeasonData.SeasonKey, Season> DEFAULT_SEASONS = Util.make(new IdentityHashMap<>(), (map) -> {

    });

    public static final SeasonConfigHolder DEFAULT_CONFIG_HOLDER = new SeasonConfigHolder(240000, DEFAULT_SEASONS);


    public static final Codec<SeasonConfigHolder> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.INT.optionalFieldOf("seasonCycleLength", 240000).forGetter((seasonConfigHolder) -> {
            return seasonConfigHolder.seasonCycleLength;
        }), Codec.simpleMap(SeasonData.SeasonKey.CODEC, Season.CODEC, IStringSerializable.createKeyable(SeasonData.SeasonKey.values())).fieldOf("seasons").forGetter((seasonConfigHolder) -> {
            return seasonConfigHolder.seasonKeySeasonMap;
        })).apply(builder, ((cycleLength, seasons) -> new SeasonConfigHolder(cycleLength, new IdentityHashMap<>(seasons))));
    });

    public SeasonConfigHolder(int seasonCycleLength, IdentityHashMap<SeasonData.SeasonKey, Season> seasonKeySeasonMap) {
        this.seasonCycleLength = seasonCycleLength;
        this.seasonKeySeasonMap = seasonKeySeasonMap;

        for (SeasonData.SeasonKey seasonKey : seasonKeySeasonMap.keySet()) {
            seasonKeySeasonMap.get(seasonKey).setSeasonKey(seasonKey);
        }
    }

    public int getSeasonCycleLength() {
        return seasonCycleLength;
    }

    public IdentityHashMap<SeasonData.SeasonKey, Season> getSeasonKeySeasonMap() {
        return seasonKeySeasonMap;
    }
}
