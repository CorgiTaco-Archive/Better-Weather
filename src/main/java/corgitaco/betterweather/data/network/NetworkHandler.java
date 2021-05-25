package corgitaco.betterweather.data.network;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.data.network.packet.season.SeasonPacket;
import corgitaco.betterweather.data.network.packet.season.SeasonTimePacket;
import corgitaco.betterweather.data.network.packet.util.RefreshRenderersPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.List;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BetterWeather.MOD_ID, "season_network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        SIMPLE_CHANNEL.registerMessage(0, SeasonPacket.class, SeasonPacket::writeToPacket, SeasonPacket::readFromPacket, SeasonPacket::handle);
        SIMPLE_CHANNEL.registerMessage(3, RefreshRenderersPacket.class, RefreshRenderersPacket::writeToPacket, RefreshRenderersPacket::readFromPacket, RefreshRenderersPacket::handle);
        SIMPLE_CHANNEL.registerMessage(4, SeasonTimePacket.class, SeasonTimePacket::writeToPacket, SeasonTimePacket::readFromPacket, SeasonTimePacket::handle);
    }

    public static void sendToPlayer(ServerPlayerEntity playerEntity, Object objectToSend) {
        SIMPLE_CHANNEL.sendTo(objectToSend, playerEntity.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAllPlayers(List<ServerPlayerEntity> playerEntities, Object objectToSend) {
        for (ServerPlayerEntity playerEntity : playerEntities) {
            SIMPLE_CHANNEL.sendTo(objectToSend, playerEntity.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendToServer(Object objectToSend) {
        SIMPLE_CHANNEL.sendToServer(objectToSend);
    }
}