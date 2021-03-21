package corgitaco.betterweather.api.season;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.util.Map;

public interface Season {

    int getYearLength();

    int getCurrentYearTime();

    Key getKey();

    Phase getPhase();

    Settings getSettings();

    enum Key implements IStringSerializable {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER;


        public static final Codec<Key> CODEC = IStringSerializable.createEnumCodec(Key::values, Key::getTypeFromId);

        private static final Map<String, Key> BY_ID = Util.make(Maps.newHashMap(), (nameToTypeMap) -> {
            for (Key key : values()) {
                nameToTypeMap.put(key.name(), key);
            }
        });


        @Nullable
        public static Key getTypeFromId(String idIn) {
            return BY_ID.get(idIn);
        }

        public static boolean hasType(String value) {
            return BY_ID.containsKey(value);
        }

        @Override
        public String getString() {
            return this.name();
        }
    }

    enum Phase implements IStringSerializable {
        START,
        MID,
        END;

        public static final Codec<Phase> CODEC = IStringSerializable.createEnumCodec(Phase::values, Phase::getTypeFromId);

        private static final Map<String, Phase> BY_ID = Util.make(Maps.newHashMap(), (nameToTypeMap) -> {
            for (Phase phase : values()) {
                nameToTypeMap.put(phase.name(), phase);
            }
        });

        @Nullable
        public static Phase getTypeFromId(String idIn) {
            return BY_ID.get(idIn);
        }

        public static boolean hasType(String value) {
            return BY_ID.containsKey(value);
        }

        @Override
        public String getString() {
            return this.name();
        }
    }


    static int getPhaseLength(int seasonLength) {
        return seasonLength / Season.Phase.values().length;
    }

    static int getSubSeasonLength(int yearLength) {
        return yearLength / (Season.Key.values().length);
    }

    static int getTimeInCycleForSeason(Season.Key seasonKey, int yearLength) {
        int perSubSeasonLength = getSubSeasonLength(yearLength);
        return perSubSeasonLength * seasonKey.ordinal();
    }


    static int getTimeInCycleForSeasonAndPhase(Season.Key seasonKey, Season.Phase phase, int yearLength) {
        int timeInCycleForSeason = getTimeInCycleForSeason(seasonKey, yearLength);
        int phaseTime = getSubSeasonLength(yearLength) / Season.Phase.values().length;
        return timeInCycleForSeason + (phaseTime * phase.ordinal());
    }


    static Season.Key getSeasonFromTime(int currentYearTime, int yearLength) {
        int seasonLength = yearLength / 4;

        if (currentYearTime < seasonLength) {
            return Season.Key.SPRING;
        } else if (currentYearTime < seasonLength * 2) {
            return Season.Key.SUMMER;
        } else if (currentYearTime < seasonLength * 3) {
            return Season.Key.AUTUMN;
        } else
            return Season.Key.WINTER;
    }
}
