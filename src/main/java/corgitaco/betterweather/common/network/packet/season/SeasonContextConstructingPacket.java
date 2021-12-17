package corgitaco.betterweather.common.network.packet.season;

import corgitaco.betterweather.util.BetterWeatherWorldData;
import corgitaco.betterweather.util.BiomeUpdate;
import corgitaco.betterweather.common.season.SeasonContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

public class SeasonContextConstructingPacket {

    private final SeasonContext seasonContext;

    public SeasonContextConstructingPacket(SeasonContext seasonContext) {
        this.seasonContext = seasonContext;
    }

    public static void writeToPacket(SeasonContextConstructingPacket packet, PacketBuffer buf) {
        try {
            buf.writeWithCodec(SeasonContext.PACKET_CODEC, packet.seasonContext);
        } catch (IOException e) {
            throw new IllegalStateException("Season packet could not be written to. This is really really bad...\n\n" + e.getMessage());

        }
    }

    public static SeasonContextConstructingPacket readFromPacket(PacketBuffer buf) {
        try {
            return new SeasonContextConstructingPacket(buf.readWithCodec(SeasonContext.PACKET_CODEC));
        } catch (IOException e) {
            throw new IllegalStateException("Season packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    public static void handle(SeasonContextConstructingPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.level;
                if (world != null && minecraft.player != null) {
                    SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
                    if (seasonContext == null) {
                        ((BetterWeatherWorldData) world).setSeasonContext(new SeasonContext(world, message.seasonContext.getYearLength(), message.seasonContext.getCropFavoriteBiomeBonuses(), message.seasonContext.getSeasons()));
                        ((BiomeUpdate) world).updateBiomeData();
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}