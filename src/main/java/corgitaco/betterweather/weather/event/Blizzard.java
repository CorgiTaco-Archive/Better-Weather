package corgitaco.betterweather.weather.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class Blizzard extends WeatherEvent {

    public static final Codec<Blizzard> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(WeatherEventClient.CODEC.fieldOf("client_settings").forGetter((blizzard) -> {
            return blizzard.getClientSettings();
        }), Codec.DOUBLE.fieldOf("chance").forGetter(blizzard -> {
            return blizzard.getDefaultChance();
        })).apply(builder, Blizzard::new);
    });


    public Blizzard(WeatherEventClient clientSettings, double defaultChance) {
        super(clientSettings, defaultChance);
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
        return null;
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
