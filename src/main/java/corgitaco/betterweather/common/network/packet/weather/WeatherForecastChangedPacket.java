package corgitaco.betterweather.common.network.packet.weather;

import corgitaco.betterweather.util.BetterWeatherWorldData;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.common.weather.WeatherForecast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

public class WeatherForecastChangedPacket {

    private final WeatherForecast weatherForecast;

    public WeatherForecastChangedPacket(WeatherForecast weatherForecast) {
        this.weatherForecast = weatherForecast;
    }

    public static void writeToPacket(WeatherForecastChangedPacket packet, PacketBuffer buf) {
        try {
            buf.writeWithCodec(WeatherForecast.CODEC, packet.weatherForecast);
        } catch (IOException e) {
            throw new IllegalStateException("Weather Forecast packet could not be written to. This is really really bad...\n\n" + e.getMessage());

        }
    }

    public static WeatherForecastChangedPacket readFromPacket(PacketBuffer buf) {
        try {
            return new WeatherForecastChangedPacket(buf.readWithCodec(WeatherForecast.CODEC));
        } catch (IOException e) {
            throw new IllegalStateException("Weather Forecast packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    public static void handle(WeatherForecastChangedPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.level;
                if (world != null && minecraft.player != null) {
                    WeatherContext weatherContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
                    if (weatherContext != null) {
                        weatherContext.getWeatherForecast().getForecast().clear();
                        weatherContext.getWeatherForecast().getForecast().addAll(message.weatherForecast.getForecast());
                        weatherContext.getWeatherForecast().setLastCheckedGameTime(message.weatherForecast.getLastCheckedGameTime());
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}