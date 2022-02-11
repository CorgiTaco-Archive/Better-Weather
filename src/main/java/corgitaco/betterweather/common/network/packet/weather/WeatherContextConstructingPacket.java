package corgitaco.betterweather.common.network.packet.weather;

import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import corgitaco.betterweather.util.BiomeUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

public class WeatherContextConstructingPacket {

    private final WeatherContext weatherContext;

    public WeatherContextConstructingPacket(WeatherContext weatherContext) {
        this.weatherContext = weatherContext;
    }

    public static void writeToPacket(WeatherContextConstructingPacket packet, PacketBuffer buf) {
        try {
            buf.writeWithCodec(WeatherContext.PACKET_CODEC, packet.weatherContext);
        } catch (IOException e) {
            throw new IllegalStateException("Weather packet could not be written to. This is really really bad...\n\n" + e.getMessage());

        }
    }

    public static WeatherContextConstructingPacket readFromPacket(PacketBuffer buf) {
        try {
            return new WeatherContextConstructingPacket(buf.readWithCodec(WeatherContext.PACKET_CODEC));
        } catch (IOException e) {
            throw new IllegalStateException("Weather packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    public static void handle(WeatherContextConstructingPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.level;
                if (world != null && minecraft.player != null) {
                    WeatherContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherContext();
                    if (weatherEventContext == null) {
                        weatherEventContext = ((BetterWeatherWorldData) world).setWeatherEventContext(new WeatherContext(message.weatherContext.getWeatherForecast(), message.weatherContext.getWeatherTimeSettings(), world.dimension().location(), message.weatherContext.getWeatherEvents(), true));
                        weatherEventContext.setCurrentEvent(message.weatherContext.getCurrentEvent().getKey());
                        ((BiomeUpdate) world).updateBiomeData();
                    } else {
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}