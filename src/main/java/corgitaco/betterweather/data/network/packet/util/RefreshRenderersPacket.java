package corgitaco.betterweather.data.network.packet.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("InstantiationOfUtilityClass")
public class RefreshRenderersPacket {

    public static void writeToPacket(RefreshRenderersPacket packet, PacketBuffer buf) {
    }

    public static RefreshRenderersPacket readFromPacket(PacketBuffer buf) {
        return new RefreshRenderersPacket();
    }

    public static void handle(RefreshRenderersPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != null && minecraft.player != null) {
                    minecraft.levelRenderer.allChanged();
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
