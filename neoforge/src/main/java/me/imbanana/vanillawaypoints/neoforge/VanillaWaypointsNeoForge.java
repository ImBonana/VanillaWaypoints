package me.imbanana.vanillawaypoints.neoforge;

import me.imbanana.vanillawaypoints.VanillaWaypoints;
import me.imbanana.vanillawaypoints.config.ModConfigScreenFactory;
import me.imbanana.vanillawaypoints.network.ModNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

@Mod(VanillaWaypoints.MOD_ID)
public final class VanillaWaypointsNeoForge {
    private static PayloadRegistrar registrar;

    public VanillaWaypointsNeoForge(IEventBus modBus) {
        // Run our common setup.
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(FMLClientSetupEvent.class, event ->
                    event.enqueueWork(VanillaWaypoints::init));
        } else {
            modBus.addListener(FMLCommonSetupEvent.class, event ->
                    event.enqueueWork(VanillaWaypoints::init));
        }

        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (client, parent) -> ModConfigScreenFactory.buildConfigScreen(parent)
        );

        modBus.addListener(VanillaWaypointsNeoForge::registerPayloads);
        NeoForge.EVENT_BUS.addListener(VanillaWaypointsNeoForge::onServerTickEnd);

        ModNetwork.registerServer(PacketDistributor::sendToPlayer);
    }

    private static void onServerTickEnd(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            VanillaWaypoints.onPlayerTickEvent(serverPlayer);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        registrar = event.registrar("1");
        ModNetwork.registerS2C(VanillaWaypointsNeoForge::registerSingleS2C);
    }

    private static <T extends CustomPacketPayload> void registerSingleS2C(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
            Consumer<T> handler
    ) {
        registrar.playToClient(type, streamCodec, (customPacketPayload, iPayloadContext) -> {
            handler.accept(customPacketPayload);
        });
    }
}
