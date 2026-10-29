package me.imbanana.vanillawaypoints.neoforge;

import me.imbanana.vanillawaypoints.VanillaWaypoints;
import me.imbanana.vanillawaypoints.VanillaWaypointsClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

@Mod(value = VanillaWaypoints.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = VanillaWaypoints.MOD_ID, value = Dist.CLIENT)
public class VanillaWaypointsClientNeoForge {
    public VanillaWaypointsClientNeoForge() { }

    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        VanillaWaypointsClient.render(event.getPoseStack(), event.getSubmitNodeCollector(), Minecraft.getInstance().gameRenderer.mainCamera());
    }

    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        VanillaWaypointsClient.onLevelChange(Minecraft.getInstance(), Minecraft.getInstance().level);
    }

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        VanillaWaypointsClient.onLevelChange(Minecraft.getInstance(), Minecraft.getInstance().level);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        VanillaWaypointsClient.onDisconnect();
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        VanillaWaypointsClient.onChunkLoad(event.getChunk());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        VanillaWaypointsClient.onChunkUnload(event.getChunk());
    }

    @SubscribeEvent
    public static void onClientStop(GameShuttingDownEvent event) {
        VanillaWaypointsClient.onClientStopping();
    }
}
