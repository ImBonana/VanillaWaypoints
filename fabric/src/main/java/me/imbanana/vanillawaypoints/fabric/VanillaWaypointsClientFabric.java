package me.imbanana.vanillawaypoints.fabric;

import me.imbanana.vanillawaypoints.VanillaWaypointsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public final class VanillaWaypointsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> VanillaWaypointsClient.render(context.poseStack(), context.submitNodeCollector(), context.gameRenderer().mainCamera()));

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register(VanillaWaypointsClient::onLevelChange);
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> VanillaWaypointsClient.onDisconnect());
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> VanillaWaypointsClient.onChunkLoad(chunk));
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> VanillaWaypointsClient.onChunkUnload(chunk));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> VanillaWaypointsClient.onClientStopping());
    }
}
