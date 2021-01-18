package corgitaco.betterweather.datastorage.network.packet;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.helper.ViewFrustumGetter;
import corgitaco.betterweather.helper.WeatherViewFrustum;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class WeatherEventPacket {
    private final String event;

    public WeatherEventPacket(String event) {
        this.event = event;
    }

    public static void writeToPacket(WeatherEventPacket packet, PacketBuffer buf) {
        buf.writeString(packet.event);
    }

    public static WeatherEventPacket readFromPacket(PacketBuffer buf) {
        return new WeatherEventPacket(buf.readString());
    }

    public static void handle(WeatherEventPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.world != null && minecraft.player != null) {
                    BetterWeather.setWeatherData(minecraft.world);
                    if (BetterWeather.weatherData != null) {
                        BetterWeather.weatherData.setEvent(message.event);
                        if (!BetterWeather.usingOptifine)
                            ((WeatherViewFrustum) ((ViewFrustumGetter) minecraft.worldRenderer).getViewFrustum()).forceRenderDistance(WeatherData.currentWeatherEvent.forcedRenderDistance(), minecraft.player.getPosX(), minecraft.player.getPosY(), minecraft.player.getPosZ());
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}