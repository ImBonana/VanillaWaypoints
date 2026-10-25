package me.imbanana.vanillawaypoints.fabric;

import me.imbanana.vanillawaypoints.VanillaWaypoints;
import me.imbanana.vanillawaypoints.network.ModNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public final class VanillaWaypointsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        VanillaWaypoints.init();

        ModNetwork.registerServer(ServerPlayNetworking::send);
        ModNetwork.registerS2C(VanillaWaypointsFabric::registerSingleS2C);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                VanillaWaypoints.onPlayerTickEvent(player);
            }
        });
    }

    private static <T extends CustomPacketPayload> void registerSingleS2C(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
            Consumer<T> handler
    ) {
        PayloadTypeRegistry.clientboundPlay().register(type, streamCodec);
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                handler.accept(payload)
        );
    }
}
