package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;

public class None extends WeatherEvent {

    public static final Codec<None> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(WeatherEventClientSettings.CODEC.fieldOf("clientSettings").forGetter((none) -> {
            return none.getClientSettings();
        })).apply(builder, None::new);
    });

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(WeatherEvent.VALUE_COMMENTS), (map) -> {
    }), true);


    public None(WeatherEventClientSettings clientSettings) {
        super(clientSettings, "", 0.0, NO_SEASON_CHANCES);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {

    }

    @Override
    public Codec<? extends WeatherEvent> codec() {
        return CODEC;
    }

    @Override
    public DynamicOps<?> configOps() {
        return CONFIG_OPS;
    }

    @Override
    public double getTemperatureModifierAtPosition(BlockPos pos) {
        return 0;
    }

    @Override
    public double getHumidityModifierAtPosition(BlockPos pos) {
        return 0;
    }
}
