package me.imbanana.vanillawaypoints.fabric;

import me.imbanana.vanillawaypoints.VanillaWaypointsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public final class VanillaWaypointsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> VanillaWaypointsClient.render(context.poseStack(), context.submitNodeCollector(), context.gameRenderer().mainCamera()));
    }
}
