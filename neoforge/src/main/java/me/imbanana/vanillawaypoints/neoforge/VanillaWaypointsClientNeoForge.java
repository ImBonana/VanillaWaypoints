package me.imbanana.vanillawaypoints.neoforge;

import me.imbanana.vanillawaypoints.VanillaWaypoints;
import me.imbanana.vanillawaypoints.VanillaWaypointsClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = VanillaWaypoints.MOD_ID, dist = Dist.CLIENT)
public class VanillaWaypointsClientNeoForge {
    public VanillaWaypointsClientNeoForge() {
        NeoForge.EVENT_BUS.addListener(this::onSubmitCustomGeometry);
    }

    private void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        VanillaWaypointsClient.render(event.getPoseStack(), event.getSubmitNodeCollector(), Minecraft.getInstance().gameRenderer.mainCamera());
    }
}
