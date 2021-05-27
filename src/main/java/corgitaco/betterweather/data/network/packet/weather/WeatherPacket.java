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

public class WeatherPacket {

    private final BWWeatherEventContext bwWeatherEventContext;

    public WeatherPacket(BWWeatherEventContext bwWeatherEventContext) {
        this.bwWeatherEventContext = bwWeatherEventContext;
    }

    public static void writeToPacket(WeatherPacket packet, PacketBuffer buf) {
        try {
            buf.func_240629_a_(BWWeatherEventContext.PACKET_CODEC, packet.bwWeatherEventContext);
        } catch (IOException e) {
            throw new IllegalStateException("Weather packet could not be written to. This is really really bad...\n\n" + e.getMessage());

        }
    }

    public static WeatherPacket readFromPacket(PacketBuffer buf) {
        try {
            return new WeatherPacket(buf.func_240628_a_(BWWeatherEventContext.PACKET_CODEC));
        } catch (IOException e) {
            throw new IllegalStateException("Weather packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    public static void handle(WeatherPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.world;
                if (world != null && minecraft.player != null) {
                    BWWeatherEventContext BWWeatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
                    if (BWWeatherEventContext == null) {
                        BWWeatherEventContext = ((BetterWeatherWorldData) world).setWeatherEventContext(new BWWeatherEventContext(message.bwWeatherEventContext.getCurrentWeatherEventKey(),
                                world.getDimensionKey().getLocation(), world.func_241828_r().getRegistry(Registry.BIOME_KEY), message.bwWeatherEventContext.getWeatherEvents()));
                        ((BiomeUpdate) world).updateBiomeData();
                    }

                    assert BWWeatherEventContext != null;
                    BWWeatherEventContext.setCurrentEvent(message.bwWeatherEventContext.getCurrentEvent());
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}