package corgitaco.betterweather.api;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Contains information in regards to the current subseason & season
 */
public class SeasonData {

    public enum SeasonKey implements IStringSerializable {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER;
        public static final Codec<SeasonKey> CODEC = IStringSerializable.createEnumCodec(SeasonKey::values, SeasonKey::getTypeFromId);


        private static final Map<String, SeasonKey> BY_ID = Util.make(Maps.newHashMap(), (nameToTypeMap) -> {
            for (SeasonKey seasonKey : values()) {
                nameToTypeMap.put(seasonKey.name(), seasonKey);
            }
        });


        @Nullable
        public static SeasonKey getTypeFromId(String idIn) {
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

    public enum Phase implements IStringSerializable {
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
}
