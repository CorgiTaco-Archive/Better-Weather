package corgitaco.betterweather.datastorage.network.packet;

import corgitaco.betterweather.datastorage.SeasonSavedData;
import corgitaco.betterweather.helper.BetterWeatherWorldData;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SeasonPacket {
    private final int currentYearTime;
    private final int yearLength;

    public SeasonPacket(int currentYearTime, int yearLength) {
        this.currentYearTime = currentYearTime;
        this.yearLength = yearLength;
    }

    public static void writeToPacket(SeasonPacket packet, PacketBuffer buf) {
        buf.writeInt(packet.currentYearTime);
        buf.writeInt(packet.yearLength);
    }

    public static SeasonPacket readFromPacket(PacketBuffer buf) {
        return new SeasonPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(SeasonPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                if (minecraft.world != null && minecraft.player != null) {
                    SeasonContext seasonContext = ((BetterWeatherWorldData) minecraft.world).getSeasonContext();
                    if (seasonContext == null) {
                        SeasonSavedData.get(minecraft.world).setCurrentYearTime(message.currentYearTime);
                        SeasonSavedData.get(minecraft.world).setYearLength(message.yearLength);
                        ((BetterWeatherWorldData) minecraft.world).setSeasonContext(new SeasonContext(SeasonSavedData.get(minecraft.world), minecraft.world.getDimensionKey(), minecraft.world.func_241828_r().getRegistry(Registry.BIOME_KEY)));
                    }

                    ((BetterWeatherWorldData) minecraft.world).getSeasonContext().setCurrentYearTime(message.currentYearTime);
                    ((BetterWeatherWorldData) minecraft.world).getSeasonContext().setYearLength(message.yearLength);
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}