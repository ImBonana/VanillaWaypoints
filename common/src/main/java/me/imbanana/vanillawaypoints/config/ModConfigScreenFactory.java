package me.imbanana.vanillawaypoints.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreenFactory {
    public static Screen buildConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(ModConfig.HANDLER, (defaults, config, builder) ->
                builder.title(Component.literal("Vanilla Waypoints"))
                        .category(ConfigCategory.createBuilder()
                                .name(Component.literal("Vanilla Waypoints"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Show Beacon Beam"))
                                        .binding(defaults.shouldShowBeaconBeam(), config::shouldShowBeaconBeam, config::setShowBeaconBeam)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build()
                                )
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.literal("Beacon Beam Hide Distance"))
                                        .binding(defaults.getBeaconBeamHideDistance(), config::getBeaconBeamHideDistance, config::setBeaconBeamHideDistance)
                                        .controller(option -> IntegerFieldControllerBuilder.create(option)
                                                .formatValue(value -> Component.literal("%s Block(s)".formatted(value)))
                                                .min(0)
                                                .max(100)
                                        )
                                        .build()
                                )
                                .option(Option.<Float>createBuilder()
                                        .name(Component.literal("Text Scale"))
                                        .binding(defaults.getWaypointTextScale(), config::getWaypointTextScale, config::setWaypointTextScale)
                                        .controller(option -> FloatSliderControllerBuilder.create(option)
                                                .formatValue(value -> Component.literal("%.2f".formatted(value * 100) + "%"))
                                                .range(0.01f, 2f)
                                                .step(0.01f)
                                        )
                                        .build()
                                )
                                .build()
                        )
        ).generateScreen(parent);
    }
}
