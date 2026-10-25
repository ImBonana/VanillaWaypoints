package me.imbanana.vanillawaypoints.util;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;

import java.util.Map;

public class ExplorerMaps {
    public static final Map<Holder<MapDecorationType>, ExplorerMap> EXPLORER_MAPS = Map.ofEntries(
            Map.entry(MapDecorationTypes.WOODLAND_MANSION, new ExplorerMap(Component.literal("Woodland Mansion"), 0xFF453530)),
            Map.entry(MapDecorationTypes.OCEAN_MONUMENT, new ExplorerMap(Component.literal("Ocean Monument"), 0xFF03fcbe)),
            Map.entry(MapDecorationTypes.RED_X, new ExplorerMap(Component.literal("Buried Treasure"), CommonColors.RED)),
            Map.entry(MapDecorationTypes.DESERT_VILLAGE, new ExplorerMap(Component.literal("Desert Village"), 0xFFded387)),
            Map.entry(MapDecorationTypes.PLAINS_VILLAGE, new ExplorerMap(Component.literal("Plains Village"), 0xFF3ba832)),
            Map.entry(MapDecorationTypes.SAVANNA_VILLAGE, new ExplorerMap(Component.literal("Savanna Village"), 0xFFad6410)),
            Map.entry(MapDecorationTypes.SNOWY_VILLAGE, new ExplorerMap(Component.literal("Snowy Village"), CommonColors.WHITE)),
            Map.entry(MapDecorationTypes.TAIGA_VILLAGE, new ExplorerMap(Component.literal("Taiga Village"), 0xFF5F363E)),
            Map.entry(MapDecorationTypes.JUNGLE_TEMPLE, new ExplorerMap(Component.literal("Jungle Temple"), 0xFF335c0a)),
            Map.entry(MapDecorationTypes.SWAMP_HUT, new ExplorerMap(Component.literal("Swamp Hut"), 0xFF3f611c)),
            Map.entry(MapDecorationTypes.TRIAL_CHAMBERS, new ExplorerMap(Component.literal("Trial Chamber"), 0xFFf77d36)),
            Map.entry(MapDecorationTypes.ABANDONED_CAMP, new ExplorerMap(Component.literal("Abandoned Camp"), CommonColors.WHITE)),
            Map.entry(MapDecorationTypes.ANCIENT_CITY, new ExplorerMap(Component.literal("Ancient City"), 0xFF0e6b82)),
            Map.entry(MapDecorationTypes.DESERT_PYRAMID, new ExplorerMap(Component.literal("Desert Pyramid"), 0xFFe0d687)),
            Map.entry(MapDecorationTypes.MINESHAFT, new ExplorerMap(Component.literal("Mineshaft"), CommonColors.WHITE)),
            Map.entry(MapDecorationTypes.OCEAN_RUIN_WARM, new ExplorerMap(Component.literal("Warm Ocean Ruin"), 0xFF80ffdb))
    );

    public record ExplorerMap(Component name, int color) { }
}