package corgitaco.betterweather.data.network.packet.season;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SeasonTimePacket {

    private final int currentSeasonYearTime;

    public SeasonTimePacket(int currentSeasonYearTime) {
        this.currentSeasonYearTime = currentSeasonYearTime;
    }

    public static void writeToPacket(SeasonTimePacket packet, PacketBuffer buf) {
        buf.writeInt(packet.currentSeasonYearTime);
    }

    public static SeasonTimePacket readFromPacket(PacketBuffer buf) {
        return new SeasonTimePacket(buf.readInt());
    }

    @SuppressWarnings("ConstantConditions")
    public static void handle(SeasonTimePacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.world;
                if (world != null && minecraft.player != null) {
                    SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
                    seasonContext.setCurrentYearTime(message.currentSeasonYearTime);
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
