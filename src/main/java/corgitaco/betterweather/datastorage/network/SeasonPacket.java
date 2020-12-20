package corgitaco.betterweather.datastorage.network;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SeasonPacket {
    private final int seasonTime;

    public SeasonPacket(int seasonTime) {
        this.seasonTime = seasonTime;
    }

    public static void writeToPacket(SeasonPacket packet, PacketBuffer buf) {
        buf.writeInt(packet.seasonTime);
    }

    public static SeasonPacket readFromPacket(PacketBuffer buf) {
        return new SeasonPacket(buf.readInt());
    }

    public static void handle(SeasonPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                if (minecraft.world != null) {
                    BetterWeather.setSeasonData(minecraft.world);
                    BetterWeather.seasonData.setSeasonTime(message.seasonTime);
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}