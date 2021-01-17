package corgitaco.betterweather.datastorage.network.packet;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class GeneralPacket {
    private final boolean usingSeasons;

    public GeneralPacket(boolean usingSeasons) {
        this.usingSeasons = usingSeasons;
    }

    public static void writeToPacket(GeneralPacket packet, PacketBuffer buf) {
        buf.writeBoolean(packet.usingSeasons);
    }

    public static GeneralPacket readFromPacket(PacketBuffer buf) {
        return new GeneralPacket(buf.readBoolean());
    }

    public static void handle(GeneralPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.world != null && minecraft.player != null) {
                    BetterWeather.setGeneralData(minecraft.world);
                    BetterWeather.generalData.setUsingSeasons(message.usingSeasons);
                    BetterWeather.useSeasons = BetterWeather.generalData.isUsingSeasons();
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
