package corgitaco.betterweather.datastorage.network.packet;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SeasonPacket {
    private final int seasonTime;
    private final int seasonCycleLength;

    public SeasonPacket(int seasonTime, int seasonCycleLength) {
        this.seasonTime = seasonTime;
        this.seasonCycleLength = seasonCycleLength;
    }

    public static void writeToPacket(SeasonPacket packet, PacketBuffer buf) {
        buf.writeInt(packet.seasonTime);
        buf.writeInt(packet.seasonCycleLength);
    }

    public static SeasonPacket readFromPacket(PacketBuffer buf) {
        return new SeasonPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(SeasonPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                if (minecraft.world != null && minecraft.player != null) {
                    BetterWeather.setSeasonData(minecraft.world);
                    if (BetterWeather.seasonData != null) {
                        BetterWeather.seasonData.setSeasonTime(message.seasonTime);
                        BetterWeather.seasonData.setSeasonCycleLength(message.seasonCycleLength);
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}