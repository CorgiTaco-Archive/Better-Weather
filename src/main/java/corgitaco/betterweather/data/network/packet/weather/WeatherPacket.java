package corgitaco.betterweather.data.network.packet.weather;

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

public class WeatherPacket {

    private final SeasonContext seasonContext;

    public WeatherPacket(SeasonContext seasonContext) {
        this.seasonContext = seasonContext;
    }

    public static void writeToPacket(WeatherPacket packet, PacketBuffer buf) {
        try {
            buf.func_240629_a_(SeasonContext.PACKET_CODEC, packet.seasonContext);
        } catch (IOException e) {
            throw new IllegalStateException("Season packet could not be written to. This is really really bad...\n\n" + e.getMessage());

        }
    }

    public static WeatherPacket readFromPacket(PacketBuffer buf) {
        try {
            return new WeatherPacket(buf.func_240628_a_(SeasonContext.PACKET_CODEC));
        } catch (IOException e) {
            throw new IllegalStateException("Season packet could not be read. This is really really bad...\n\n" + e.getMessage());
        }
    }

    public static void handle(WeatherPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection().getReceptionSide().isClient()) {
            ctx.get().enqueueWork(() -> {
                Minecraft minecraft = Minecraft.getInstance();

                ClientWorld world = minecraft.world;
                if (world != null && minecraft.player != null) {
                    SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
                    if (seasonContext == null) {
                        seasonContext = ((BetterWeatherWorldData) world).setSeasonContext(new SeasonContext(message.seasonContext.getCurrentYearTime(), message.seasonContext.getYearLength(),
                                world.getDimensionKey().getLocation(), world.func_241828_r().getRegistry(Registry.BIOME_KEY), message.seasonContext.getSeasons()));
                        ((BiomeUpdate) world).updateBiomeData();
                    }

                    seasonContext.setCurrentYearTime(message.seasonContext.getCurrentYearTime());
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }

//    public static class Serializable {
//        public static final Codec<Serializable> CODEC = RecordCodecBuilder.create((builder) -> {
//            builder.group(Codec.STRING.fieldOf("currentWeatherEvent").forGetter((serializable -> {
//                return serializable.currentEvent;
//            }));
//        })
//
//
//        private final String currentEvent;
//        private final Map<String, WeatherEvent> weatherEvents;
//
//        public Serializable(String currentEvent, Map<String, WeatherEvent> weatherEvents) {
//
//            this.currentEvent = currentEvent;
//            this.weatherEvents = weatherEvents;
//        }
//    }
}