package me.imbanana.vanillawaypoints.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.phys.Vec3;

public record Waypoint(Vec3 pos, Component name, Component icon, int color) {
    public static final StreamCodec<RegistryFriendlyByteBuf, Waypoint> STREAM_CODEC = StreamCodec.composite(
            Vec3.STREAM_CODEC,
            Waypoint::pos,
            ComponentSerialization.STREAM_CODEC,
            Waypoint::name,
            ComponentSerialization.STREAM_CODEC,
            Waypoint::icon,
            ByteBufCodecs.VAR_INT,
            Waypoint::color,
            Waypoint::new
    );

    public Waypoint(BlockPos blockPos, Component name, Component icon, int color) {
        this(new Vec3(blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f), name, icon, color);
    }

    @Override
    public Component icon() {
        return icon == null ? Component.empty() : icon;
    }

    public static Component createIcon(Holder<MapDecorationType> decoration, boolean space) {
        return Component.object(new AtlasSprite(AtlasIds.MAP_DECORATIONS, decoration.value().assetId())).append(space ? " " : "");
    }
}
