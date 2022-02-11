package corgitaco.betterweather.common.network.packet.season;

import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class YearTimePacket {

    private final int yearTime;

    public YearTimePacket(int yearTime) {
        this.yearTime = yearTime;
    }

    public static void writeToPacket(YearTimePacket packet, PacketBuffer buf) {
        buf.writeVarInt(packet.yearTime);
    }

    public static YearTimePacket readFromPacket(PacketBuffer buf) {
        return new YearTimePacket(buf.readVarInt());
    }

    public static void handle(YearTimePacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.level;
                if (world != null && minecraft.player != null) {
                    SeasonContext lunarContext = ((BetterWeatherWorldData) world).getSeasonContext();
                    if (lunarContext != null) {
                        lunarContext.updateYearTime(world, message.yearTime);
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}