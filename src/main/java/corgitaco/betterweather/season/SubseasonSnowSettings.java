package corgitaco.betterweather.season;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Map;

public class SubseasonSnowSettings {

    public static final Codec<SubseasonSnowSettings> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(SnowType.CODEC.fieldOf("snowType").forGetter((subseasonSnowSettings) -> {
            return subseasonSnowSettings.snowType;
        }), Codec.DOUBLE.fieldOf("minimum").forGetter((subseasonSnowSettings) -> {
            return subseasonSnowSettings.minimum;
        })).apply(builder, SubseasonSnowSettings::new);
    });

    private final SnowType snowType;
    private final double minimum;

    public SubseasonSnowSettings(SnowType snowType, double minimum) {
        this.snowType = snowType;
        this.minimum = MathHelper.clamp(minimum, 0.0, 1.0);
    }

    public SnowType getSnowType() {
        return snowType;
    }

    public double getMinimum() {
        return minimum;
    }

    public enum SnowType implements IStringSerializable {
        SPAWN,
        DECAY;

        public static final Codec<SnowType> CODEC = IStringSerializable.createEnumCodec(SnowType::values, SnowType::getTypeFromId);

        private static final Map<String, SnowType> BY_ID = Util.make(Maps.newHashMap(), (nameToTypeMap) -> {
            for (SnowType phase : values()) {
                nameToTypeMap.put(phase.name(), phase);
            }
        });

        @Nullable
        public static SnowType getTypeFromId(String idIn) {
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
