package corgitaco.betterweather.data.network.packet.weather;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class WeatherDataPacket {

    private final String weatherEvent;
    private final boolean weatherForced;

    public WeatherDataPacket(BWWeatherEventContext bwWeatherEventContext) {
        this(bwWeatherEventContext.getCurrentWeatherEventKey(), bwWeatherEventContext.isWeatherForced());
    }

    public WeatherDataPacket(String weatherEvent, boolean weatherForced) {
        this.weatherEvent = weatherEvent;
        this.weatherForced = weatherForced;
    }

    public static void writeToPacket(WeatherDataPacket packet, PacketBuffer buf) {
        buf.writeUtf(packet.weatherEvent);
        buf.writeBoolean(packet.weatherForced);
    }

    public static WeatherDataPacket readFromPacket(PacketBuffer buf) {
        return new WeatherDataPacket(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(WeatherDataPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.level;
                if (world != null && minecraft.player != null) {
                    BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
                    if (weatherEventContext == null) {
                        throw new UnsupportedOperationException("There is no weather event context constructed for this world!");
                    } else {
                        weatherEventContext.setCurrentEvent(message.weatherEvent);
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}