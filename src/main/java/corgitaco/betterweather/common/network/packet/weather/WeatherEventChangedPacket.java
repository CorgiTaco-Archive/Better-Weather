package corgitaco.betterweather.common.network.packet.weather;

import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class WeatherEventChangedPacket {

    private final String eventKey;

    public WeatherEventChangedPacket(String eventKey) {
        this.eventKey = eventKey;
    }

    public static void writeToPacket(WeatherEventChangedPacket packet, PacketBuffer buf) {
        buf.writeUtf(packet.eventKey);
    }

    public static WeatherEventChangedPacket readFromPacket(PacketBuffer buf) {
        return new WeatherEventChangedPacket(buf.readUtf());
    }

    public static void handle(WeatherEventChangedPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.level;
                if (world != null && minecraft.player != null) {
                    WeatherContext weatherContext = ((BetterWeatherWorldData) world).getWeatherContext();
                    if (weatherContext != null) {
                        weatherContext.setLastEvent(weatherContext.getCurrentEvent());
                        weatherContext.setCurrentEvent(message.eventKey);
                        weatherContext.setStrength(0);
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}