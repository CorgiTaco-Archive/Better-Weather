package corgitaco.betterweather.datastorage.network.packet;

import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("InstantiationOfUtilityClass")
public class OnCommandWeatherChangePacket {

    private final String previousWeatherEventID;

    public OnCommandWeatherChangePacket(String previousWeatherEventID) {
        this.previousWeatherEventID = previousWeatherEventID;
    }

    public static void writeToPacket(OnCommandWeatherChangePacket packet, PacketBuffer buf) {
        buf.writeString(packet.previousWeatherEventID);
    }

    public static OnCommandWeatherChangePacket readFromPacket(PacketBuffer buf) {
        return new OnCommandWeatherChangePacket(buf.readString());
    }

    public static void handle(OnCommandWeatherChangePacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.world != null && minecraft.player != null) {
                    if (WeatherEventSystem.WEATHER_EVENTS.containsKey(new BetterWeatherID(message.previousWeatherEventID))) {
                        WeatherEventSystem.WEATHER_EVENTS.get(new BetterWeatherID(message.previousWeatherEventID)).onCommandWeatherChange();
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
