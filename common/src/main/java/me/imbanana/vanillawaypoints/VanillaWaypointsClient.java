package me.imbanana.vanillawaypoints;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.imbanana.vanillawaypoints.config.ModConfig;
import me.imbanana.vanillawaypoints.util.ExplorerMaps;
import me.imbanana.vanillawaypoints.util.Waypoint;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CommonColors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VanillaWaypointsClient {
    private static final List<Waypoint> serverWaypoints = new ArrayList<>();

    public static void updateWaypointsFromServer(List<Waypoint> waypoints) {
        serverWaypoints.clear();
        serverWaypoints.addAll(waypoints);
    }

    public static void render(PoseStack pose, SubmitNodeCollector nodeCollector, Camera camera) {
        List<Waypoint> waypoints = getWaypointsFromPlayer();

        for (Waypoint waypoint : waypoints) {
            renderWaypoint(waypoint, pose, nodeCollector, camera);
        }
    }

    private static void renderWaypoint(Waypoint waypoint, PoseStack pose, SubmitNodeCollector nodeCollector, Camera camera) {
        ModConfig config = ModConfig.HANDLER.instance();

        pose.pushPose();

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        Vec3 cameraDist = waypoint.pos().subtract(camera.position());
        Vec3 pointDir = cameraDist.normalize();
        double distanceToCamera = waypoint.pos().distanceTo(camera.position());
        int renderDistance = minecraft.options.getEffectiveRenderDistance() * 8;

        if (distanceToCamera > renderDistance) {
            double modifier = renderDistance / distanceToCamera;
            cameraDist = cameraDist.scale(modifier);
        }

        pose.translate(cameraDist.x(), cameraDist.y(), cameraDist.z());
        pose.rotate(Axis.YP.rotationDegrees(-camera.yRot()));
        pose.rotate(Axis.XP.rotationDegrees(camera.xRot()));

        Vec3 look = player.getViewVector(1f);
        double angle = Math.toDegrees(Math.acos(Math.clamp(look.dot(pointDir), -1f, 1f)));
        float lookProgress = (float) Math.clamp(1f - angle / 10f, 0f, 1f);

        float scale = (float) (Math.clamp(distanceToCamera, 1f, renderDistance) * 0.005f) * (1 + lookProgress * 0.2f) * config.getWaypointTextScale();

        pose.scale(-scale, -scale, scale);

        Component text = waypoint.icon().copy().append(Component.literal("[%sm] ".formatted((int) distanceToCamera)).append(waypoint.name()).withColor(waypoint.color()));
        float width = minecraft.font.width(text);
        int backgroundColor = (int) (minecraft.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;

        nodeCollector.submitText(pose, -width / 2f, 0, text.getVisualOrderText(), false, Font.DisplayMode.SEE_THROUGH, 15, CommonColors.WHITE, backgroundColor, 0);

        pose.popPose();

        if (config.shouldShowBeaconBeam() && player.position().subtract(waypoint.pos()).horizontalDistanceSqr() > config.getBeaconBeamHideDistance() * config.getBeaconBeamHideDistance()) {
            pose.pushPose();

            if (distanceToCamera > renderDistance) {
                pose.translate(cameraDist.x() * 2, player.level().getMinY() - camera.position().y(), cameraDist.z() * 2);
            } else {
                pose.translate(-camera.position().x(), -camera.position().y(), -camera.position().z());
                pose.translate(Math.floor(waypoint.pos().x()), player.level().getMinY(), Math.floor(waypoint.pos().z()));
            }

            BeaconRenderer.submitBeaconBeam(pose, nodeCollector, BeaconRenderer.BEAM_LOCATION, 1f, Math.floorMod(player.level().getGameTime(), 40) + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false), 0, 2048, waypoint.color(), 0.2f, 0.25f);

            pose.popPose();
        }
    }

    private static List<Waypoint> getWaypointsFromPlayer() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack mainItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offItem = player.getItemInHand(InteractionHand.OFF_HAND);

        List<Waypoint> waypoints = getWaypointsFromItem(mainItem, player);
        waypoints.addAll(getWaypointsFromItem(offItem, player));
        waypoints.addAll(serverWaypoints);

        return waypoints;
    }

    private static List<Waypoint> getWaypointsFromItem(ItemStack itemStack, LocalPlayer player) {
        List<Waypoint> waypoints = new ArrayList<>();

        ResourceKey<Level> playerDimension = player.level().dimension();

        if (itemStack.is(Items.COMPASS)) {
            GlobalPos globalPos = player.level().getRespawnData().globalPos();
            Component name = Component.literal("World Spawn");

            if (itemStack.has(DataComponents.LODESTONE_TRACKER)) {
                LodestoneTracker lodestoneTracker = itemStack.get(DataComponents.LODESTONE_TRACKER);
                if (lodestoneTracker.target().isPresent()) {
                    globalPos = lodestoneTracker.target().get();
                    name = itemStack.getCustomName() != null ? itemStack.getCustomName() : Component.literal("Lodestone");
                }
            }

            if (globalPos.dimension().equals(playerDimension)) {
                waypoints.add(new Waypoint(globalPos.pos(), name, Waypoint.createIcon(MapDecorationTypes.TARGET_X, true), CommonColors.WHITE));
            }
        } else if (itemStack.is(Items.RECOVERY_COMPASS)) {
            Optional<GlobalPos> lastDeathLocationOptional = player.getLastDeathLocation();

            if (lastDeathLocationOptional.isPresent()) {
                GlobalPos lastDeathLocation = lastDeathLocationOptional.get();

                if (lastDeathLocation.dimension().equals(playerDimension)) {
                    waypoints.add(new Waypoint(lastDeathLocation.pos(), Component.literal("Last Death"), Waypoint.createIcon(MapDecorationTypes.RED_X, true), CommonColors.SOFT_RED));
                }
            }
        } else if (itemStack.has(DataComponents.BUNDLE_CONTENTS)) {
            BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);

            for (ItemStack item : bundleContents.itemCopies().toList()) {
                waypoints.addAll(getWaypointsFromItem(item, player));
            }
        } else if (itemStack.is(Items.SHULKER_BOX) || Items.DYED_SHULKER_BOX.asList().stream().anyMatch(itemStack::is)) {
            if (itemStack.has(DataComponents.CONTAINER)) {
                ItemContainerContents itemContainerContents = itemStack.get(DataComponents.CONTAINER);

                for (ItemStack item : itemContainerContents.itemCopies().toList()) {
                    waypoints.addAll(getWaypointsFromItem(item, player));
                }
            }
        } else if (itemStack.has(DataComponents.MAP_ID)) {
            if (itemStack.has(DataComponents.MAP_DECORATIONS)) {
                MapDecorations mapDecorations = itemStack.get(DataComponents.MAP_DECORATIONS);

                for (Map.Entry<String, MapDecorations.Entry> entry : mapDecorations.decorations().entrySet()) {
                    ExplorerMaps.ExplorerMap explorerMap = ExplorerMaps.EXPLORER_MAPS.get(entry.getValue().type());
                    if (explorerMap == null) continue;
                    waypoints.add(new Waypoint(
                            new Vec3(entry.getValue().x(), player.getY(), entry.getValue().z()),
                            explorerMap.name(),
                            Waypoint.createIcon(entry.getValue().type(), true),
                            explorerMap.color()
                    ));
                }
            }
        }

        return waypoints;
    }
}
