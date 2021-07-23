package corgitaco.betterweather.data.network.packet.season;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.helpers.BiomeUpdate;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
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
            buf.func_240629_a_(SeasonContext.PACKET_CODEC, packet.seasonContext);
        } catch (IOException e) {
            throw new IllegalStateException("Season packet could not be written to. This is really really bad...\n\n" + e.getMessage());

        }
    }

    public static SeasonContextConstructingPacket readFromPacket(PacketBuffer buf) {
        try {
            return new SeasonContextConstructingPacket(buf.func_240628_a_(SeasonContext.PACKET_CODEC));
        } catch (IOException e) {
            throw new IllegalStateException("Season packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    public static void handle(SeasonContextConstructingPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.world;
                if (world != null && minecraft.player != null) {
                    SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
                    if (seasonContext == null) {
                        seasonContext = ((BetterWeatherWorldData) world).setSeasonContext(new SeasonContext(message.seasonContext.getCurrentYearTime(), message.seasonContext.getYearLength(),
                                world.getDimensionKey().getLocation(), message.seasonContext.getCropFavoriteBiomeBonuses(), world.func_241828_r().getRegistry(Registry.BIOME_KEY), message.seasonContext.getSeasons()));
                        ((BiomeUpdate) world).updateBiomeData();
                    }

                    seasonContext.setCurrentYearTime(message.seasonContext.getCurrentYearTime());
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}