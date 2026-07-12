package net.cravencraft.betterparagliders.network;

import net.cravencraft.betterparagliders.BetterParaglidersMod;
import net.cravencraft.betterparagliders.events.BetterParaglidersEventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNet {
    private ModNet() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(ModNet::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(BetterParaglidersMod.MOD_ID).versioned("3.0");

        registrar.playToServer(
                SyncActionToServerPayload.ID,
                SyncActionToServerPayload.STREAM_CODEC,
                ModNet::handleSyncActionToServer
        );
    }

    private static void handleSyncActionToServer(SyncActionToServerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BetterParaglidersEventHandler.handleMeleeSwingPacket(player, payload.comboCount());
            }
        });
    }
}
