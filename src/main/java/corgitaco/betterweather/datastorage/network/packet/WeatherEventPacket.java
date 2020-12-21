package corgitaco.betterweather.datastorage.network.packet;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class WeatherEventPacket {
    private final String event;
    private boolean isWeatherForced;

    public WeatherEventPacket(String event, boolean isWeatherForced) {
        this.event = event;
        this.isWeatherForced = isWeatherForced;
    }

    public static void writeToPacket(WeatherEventPacket packet, PacketBuffer buf) {
        buf.writeString(packet.event);
        buf.writeBoolean(packet.isWeatherForced);
    }

    public static WeatherEventPacket readFromPacket(PacketBuffer buf) {
        return new WeatherEventPacket(buf.readString(), buf.readBoolean());
    }

    public static void handle(WeatherEventPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                if (minecraft.world != null) {
                    BetterWeather.setWeatherData(minecraft.world);
                    BetterWeather.weatherData.setEvent(message.event);
                    BetterWeather.weatherData.setWeatherForced(message.isWeatherForced);
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}