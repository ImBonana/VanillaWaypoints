package me.imbanana.vanillawaypoints.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class ModNetwork {
    private static SendToPlayerFunction sendToPlayerFunction;

    public static void registerS2C(S2CPayloadRegistry registrar) {
        registrar.register(SyncMapWaypointsPayloadS2C.TYPE, SyncMapWaypointsPayloadS2C.STREAM_CODEC, SyncMapWaypointsPayloadS2C::handle);
    }

    public static void registerServer(SendToPlayerFunction sendToPlayerFunction) {
        ModNetwork.sendToPlayerFunction = sendToPlayerFunction;
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        sendToPlayerFunction.send(player, payload);
    }

    @FunctionalInterface
    public interface S2CPayloadRegistry {
        <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Consumer<T> handler);
    }

    @FunctionalInterface
    public interface SendToPlayerFunction {
        void send(ServerPlayer player, CustomPacketPayload payload);
    }
}
