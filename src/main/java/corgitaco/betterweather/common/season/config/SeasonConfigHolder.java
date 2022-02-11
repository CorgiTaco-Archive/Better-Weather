package corgitaco.betterweather.common.season.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.common.season.BWSeason;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class SeasonConfigHolder {

    public static final IdentityHashMap<Season.Key, BWSeason> DEFAULT_SEASONS = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(Season.Key.SPRING, BWSeason.DEFAULT_SPRING);
        map.put(Season.Key.SUMMER, BWSeason.DEFAULT_SUMMER);
        map.put(Season.Key.AUTUMN, BWSeason.DEFAULT_AUTUMN);
        map.put(Season.Key.WINTER, BWSeason.DEFAULT_WINTER);
    });

    public static final SeasonConfigHolder DEFAULT_CONFIG_HOLDER = new SeasonConfigHolder(true, 2400000, DEFAULT_SEASONS);

    public static final Codec<SeasonConfigHolder> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.BOOL.fieldOf("tickSeasonTimeWhenNoPlayersOnline").orElse(true).forGetter((seasonConfigHolder) -> {
            return seasonConfigHolder.tickSeasonTimeWhenNoPlayersOnline;
        }), Codec.INT.fieldOf("yearLength").orElse(2400000).forGetter((seasonConfigHolder) -> {
            return seasonConfigHolder.seasonCycleLength;
        }), Codec.simpleMap(Season.Key.CODEC, BWSeason.CODEC, IStringSerializable.keys(Season.Key.values())).fieldOf("seasons").forGetter((seasonConfigHolder) -> {
            return seasonConfigHolder.seasonKeySeasonMap;
        })).apply(builder, ((tickSeasonTimeWhenNoPlayersOnline, cycleLength, seasons) -> new SeasonConfigHolder(tickSeasonTimeWhenNoPlayersOnline, cycleLength, new IdentityHashMap<>(seasons))));
    });

    private final boolean tickSeasonTimeWhenNoPlayersOnline;
    private final int seasonCycleLength;
    private final EnumMap<Season.Key, BWSeason> seasonKeySeasonMap;

    public SeasonConfigHolder(boolean tickSeasonTimeWhenNoPlayersOnline, int seasonCycleLength, Map<Season.Key, BWSeason> seasonKeySeasonMap) {
        this.tickSeasonTimeWhenNoPlayersOnline = tickSeasonTimeWhenNoPlayersOnline;
        this.seasonCycleLength = seasonCycleLength;
        this.seasonKeySeasonMap = new EnumMap<>(seasonKeySeasonMap);

        for (Season.Key key : seasonKeySeasonMap.keySet()) {
            seasonKeySeasonMap.get(key).setSeasonKey(key);
        }
    }

    public boolean isTickSeasonTimeWhenNoPlayersOnline() {
        return tickSeasonTimeWhenNoPlayersOnline;
    }

    public int getYearLength() {
        return seasonCycleLength;
    }

    public Map<Season.Key, BWSeason> getSeasonKeySeasonMap() {
        return seasonKeySeasonMap;
    }
}
