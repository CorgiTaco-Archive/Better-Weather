package corgitaco.betterweather.data.network.packet.weather;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.helpers.BiomeUpdate;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

public class WeatherContextConstructingPacket {

    private final BWWeatherEventContext bwWeatherEventContext;

    public WeatherContextConstructingPacket(BWWeatherEventContext bwWeatherEventContext) {
        this.bwWeatherEventContext = bwWeatherEventContext;
    }

    public static void writeToPacket(WeatherContextConstructingPacket packet, PacketBuffer buf) {
        try {
            buf.func_240629_a_(BWWeatherEventContext.PACKET_CODEC, packet.bwWeatherEventContext);
        } catch (IOException e) {
            throw new IllegalStateException("Weather packet could not be written to. This is really really bad...\n\n" + e.getMessage());

        }
    }

    public static WeatherContextConstructingPacket readFromPacket(PacketBuffer buf) {
        try {
            return new WeatherContextConstructingPacket(buf.func_240628_a_(BWWeatherEventContext.PACKET_CODEC));
        } catch (IOException e) {
            throw new IllegalStateException("Weather packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    public static void handle(WeatherContextConstructingPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.world;
                if (world != null && minecraft.player != null) {
                    BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
                    if (weatherEventContext == null) {
                        weatherEventContext = ((BetterWeatherWorldData) world).setWeatherEventContext(new BWWeatherEventContext(message.bwWeatherEventContext.getCurrentWeatherEventKey(),
                                message.bwWeatherEventContext.isWeatherForced(), world.getDimensionKey().getLocation(), world.func_241828_r().getRegistry(Registry.BIOME_KEY), message.bwWeatherEventContext.getWeatherEvents()));
                        weatherEventContext.setCurrentEvent(message.bwWeatherEventContext.getCurrentEvent());
                        ((BiomeUpdate) world).updateBiomeData();
                    } else {
                        throw new UnsupportedOperationException("This should only ever be called for constructing the Weather Context!");
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}