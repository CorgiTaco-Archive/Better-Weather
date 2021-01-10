package corgitaco.betterweather.datastorage.network;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.network.packet.GeneralPacket;
import corgitaco.betterweather.datastorage.network.packet.RefreshRenderersPacket;
import corgitaco.betterweather.datastorage.network.packet.SeasonPacket;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        SIMPLE_CHANNEL.registerMessage(1, WeatherEventPacket.class, WeatherEventPacket::writeToPacket, WeatherEventPacket::readFromPacket, WeatherEventPacket::handle);
        SIMPLE_CHANNEL.registerMessage(2, GeneralPacket.class, GeneralPacket::writeToPacket, GeneralPacket::readFromPacket, GeneralPacket::handle);
        SIMPLE_CHANNEL.registerMessage(3, RefreshRenderersPacket.class, RefreshRenderersPacket::writeToPacket, RefreshRenderersPacket::readFromPacket, RefreshRenderersPacket::handle);
    }

    public static void sendTo(ServerPlayerEntity playerEntity, Object objectToSend) {
        SIMPLE_CHANNEL.sendTo(objectToSend, playerEntity.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }
}