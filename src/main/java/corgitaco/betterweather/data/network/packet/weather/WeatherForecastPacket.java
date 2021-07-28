package corgitaco.betterweather.data.network.packet.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import corgitaco.betterweather.weather.WeatherInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class WeatherForecastPacket {

    private final SerializableForecast forecast;

    public WeatherForecastPacket(SerializableForecast forecast) {
        this.forecast = forecast;
    }

    public WeatherForecastPacket(List<WeatherInstance> forecast) {
        this.forecast = new SerializableForecast(forecast);
    }

    public static void writeToPacket(WeatherForecastPacket packet, PacketBuffer buf) {
        try {
            buf.func_240629_a_(SerializableForecast.PACKET_CODEC, packet.forecast);
        } catch (IOException e) {
            throw new IllegalStateException("Forecast packet could not be written to. This is really really bad...\n\n" + e.getMessage());

        }
    }

    public static WeatherForecastPacket readFromPacket(PacketBuffer buf) {
        try {
            return new WeatherForecastPacket(buf.func_240628_a_(SerializableForecast.PACKET_CODEC));
        } catch (IOException e) {
            throw new IllegalStateException("Forecast packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    public static void handle(WeatherForecastPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.world;
                if (world != null && minecraft.player != null) {
                    BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
                    if (weatherEventContext == null) {
                        throw new UnsupportedOperationException("There is no weather event context constructed for this world!");
                    } else {
                        weatherEventContext.getForecast().clear();
                        weatherEventContext.getForecast().addAll(message.forecast.getForecast());
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }

    public static class SerializableForecast {

        public static final Codec<SerializableForecast> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
            return builder.group(Codec.list(WeatherInstance.PACKET_CODEC).fieldOf("forecast").forGetter((weatherInstance) -> {
                return weatherInstance.forecast;
            })).apply(builder, SerializableForecast::new);
        });
        private final List<WeatherInstance> forecast;

        public SerializableForecast(List<WeatherInstance> forecast) {
            this.forecast = forecast;
        }

        public List<WeatherInstance> getForecast() {
            return forecast;
        }
    }
}