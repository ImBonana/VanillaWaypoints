package me.imbanana.vanillawaypoints;

import me.imbanana.vanillawaypoints.config.ModConfig;
import me.imbanana.vanillawaypoints.network.ModNetwork;
import me.imbanana.vanillawaypoints.network.SyncMapWaypointsPayloadS2C;
import me.imbanana.vanillawaypoints.util.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class VanillaWaypoints {
    public static final String MOD_ID = "vanillawaypoints";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ModConfig.HANDLER.load();
    }

    public static void onPlayerTickEvent(ServerPlayer player) {
        if (player.tickCount % 20 == 0) {
            ModNetwork.sendToPlayer(player, new SyncMapWaypointsPayloadS2C(getWaypointsFromPlayer(player)));
        }
    }

    private static List<Waypoint> getWaypointsFromPlayer(ServerPlayer player) {
        ItemStack mainItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offItem = player.getItemInHand(InteractionHand.OFF_HAND);

        List<Waypoint> waypoints = getWaypointsFromItem(mainItem, player);
        waypoints.addAll(getWaypointsFromItem(offItem, player));

        return waypoints;
    }

    private static List<Waypoint> getWaypointsFromItem(ItemStack itemStack, ServerPlayer player) {
        List<Waypoint> waypoints = new ArrayList<>();

        if (itemStack.has(DataComponents.MAP_ID)) {
            MapItemSavedData data = MapItem.getSavedData(itemStack.get(DataComponents.MAP_ID), player.level());

            if (data != null && data.dimension.equals(player.level().dimension())) {
                waypoints.addAll(data.getBanners().stream().map(mapBanner -> new Waypoint(mapBanner.pos(), mapBanner.name().orElse(Component.empty()), Waypoint.createIcon(mapBanner.getDecoration(), false), mapBanner.color().getTextColor())).toList());
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
        }

        return waypoints;
    }


    public static Identifier idOf(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Path getConfigDir() {
        Path configDir = Minecraft.getInstance().gameDirectory.toPath().resolve("config");

        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                throw new RuntimeException("Creating config directory", e);
            }
        }

        return configDir;
    }
}
