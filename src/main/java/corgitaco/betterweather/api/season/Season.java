package corgitaco.betterweather.api.season;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import corgitaco.betterweather.api.Climate;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Contains season data for a world including the current year time, year length, the key, phase, and settings for a world.
 */
public interface Season {

    /**
     * If null, seasons are not enabled for this world.
     * @param world Should be either or extenders of {@link net.minecraft.client.world.ClientWorld} or {@link net.minecraft.world.server.ServerWorld}
     * @return Season
     */
    @Nullable
    static Season getSeason(World world) {
        return ((Climate) world).getSeason();
    }

    /**
     * @return total year length.
     */
    int getYearLength();

    /**
     * @return current year time.
     */
    int getCurrentYearTime();

    /**
     * @return current season's key.
     */
    Key getKey();

    /**
     * @return current season's phase/subseason
     */
    Phase getPhase();

    /**
     * @return current subseason's settings.
     * In the current implementation, each season has 3 (length of {@link Phase}) subseason settings for each phase.
     */
    SubseasonSettings getSettings();

    /**
     * @return start time for this season in the given year.
     */
    default int getSeasonStartTime() {
        return getSeasonStartTime(getKey(), getYearLength());
    }

    /**
     * @return start time for this season's phase in the given year.
     */
    default int getSeasonAndPhaseStartTime() {
        return getSeasonAndPhaseStartTime(getKey(), getPhase(), getYearLength());
    }

    /**
     * Represents the given season key or "name".
     */
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

    /**
     * Represents both the time within a given season and the "Subseason".
     */
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


    /**
     * @return total length of a given phase as determined by the given season length.
     */
    static int getPhaseLength(int seasonLength) {
        return seasonLength / Phase.values().length;
    }

    /**
     * @return total length of a given season as determined by the given year length.
     */
    static int getSeasonLength(int yearLength) {
        return yearLength / (Key.values().length);
    }

    /**
     * @return start time for the given season & year length.
     */
    static int getSeasonStartTime(Key seasonKey, int yearLength) {
        int perSubSeasonLength = getSeasonLength(yearLength);
        return perSubSeasonLength * seasonKey.ordinal();
    }

    /**
     * @return start time of the given season's phase for the given year length.
     */
    static int getSeasonAndPhaseStartTime(Key seasonKey, Phase phase, int yearLength) {
        int timeInCycleForSeason = getSeasonStartTime(seasonKey, yearLength);
        int phaseTime = getSeasonLength(yearLength) / Phase.values().length;
        return timeInCycleForSeason + (phaseTime * phase.ordinal());
    }

    /**
     * @return current season key determined by the year's current time and length.
     */
    static Key getSeasonFromTime(int currentYearTime, int yearLength) {
        int seasonLength = yearLength / Key.values().length;

        if (currentYearTime < seasonLength) {
            return Key.SPRING;
        } else if (currentYearTime < seasonLength * 2) {
            return Key.SUMMER;
        } else if (currentYearTime < seasonLength * 3) {
            return Key.AUTUMN;
        } else
            return Key.WINTER;
    }
}
