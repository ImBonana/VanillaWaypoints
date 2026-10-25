package me.imbanana.vanillawaypoints.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import me.imbanana.vanillawaypoints.VanillaWaypoints;

public class ModConfig {
    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(VanillaWaypoints.idOf("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(VanillaWaypoints.getConfigDir().resolve(VanillaWaypoints.MOD_ID + ".json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build()
            )
            .build();

    @SerialEntry
    private boolean showBeaconBeam = true;

    @SerialEntry
    private int beaconBeamHideDistance = 5;

    @SerialEntry
    private float waypointTextScale = 1f;

    public boolean shouldShowBeaconBeam() {
        return showBeaconBeam;
    }

    public void setShowBeaconBeam(boolean showBeaconBeam) {
        this.showBeaconBeam = showBeaconBeam;
    }

    public int getBeaconBeamHideDistance() {
        return beaconBeamHideDistance;
    }

    public void setBeaconBeamHideDistance(int beaconBeamHideDistance) {
        this.beaconBeamHideDistance = beaconBeamHideDistance;
    }

    public float getWaypointTextScale() {
        return waypointTextScale;
    }

    public void setWaypointTextScale(float waypointTextScale) {
        this.waypointTextScale = waypointTextScale;
    }
}
