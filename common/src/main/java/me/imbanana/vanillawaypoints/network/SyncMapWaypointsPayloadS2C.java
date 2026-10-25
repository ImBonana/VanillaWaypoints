package me.imbanana.vanillawaypoints.network;

import me.imbanana.vanillawaypoints.VanillaWaypoints;
import me.imbanana.vanillawaypoints.VanillaWaypointsClient;
import me.imbanana.vanillawaypoints.util.Waypoint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record SyncMapWaypointsPayloadS2C(List<Waypoint> waypoints) implements CustomPacketPayload {
    public static final Identifier ID = VanillaWaypoints.idOf("sync_map_waypoints");
    public static final CustomPacketPayload.Type<SyncMapWaypointsPayloadS2C> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMapWaypointsPayloadS2C> STREAM_CODEC = StreamCodec.composite(
            Waypoint.STREAM_CODEC.apply(ByteBufCodecs.list(256)),
            SyncMapWaypointsPayloadS2C::waypoints,
            SyncMapWaypointsPayloadS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(SyncMapWaypointsPayloadS2C payload) {
        VanillaWaypointsClient.updateWaypointsFromServer(payload.waypoints());
    }
}
