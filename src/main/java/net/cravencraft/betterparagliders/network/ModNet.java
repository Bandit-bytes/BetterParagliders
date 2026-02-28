package net.cravencraft.betterparagliders.network;

import net.cravencraft.betterparagliders.BetterParaglidersMod;
import net.cravencraft.betterparagliders.capabilities.StaminaOverride;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tictim.paraglider.api.stamina.Stamina;

public final class ModNet {
    private ModNet() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(ModNet::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(BetterParaglidersMod.MOD_ID).versioned("2.0");

        registrar.playToClient(
                SyncActionToClientPayload.ID,
                SyncActionToClientPayload.STREAM_CODEC,
                ModNet::handleSyncActionToClient
        );

        registrar.playToServer(
                SyncActionToServerPayload.ID,
                SyncActionToServerPayload.STREAM_CODEC,
                ModNet::handleSyncActionToServer
        );
    }

    public static void sendToPlayer(ServerPlayer player, SyncActionToClientPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    private static void handleSyncActionToClient(SyncActionToClientPayload payload, IPayloadContext ctx) {
    }

    private static void handleSyncActionToServer(SyncActionToServerPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            Stamina stamina = Stamina.get(player);
            if (stamina == null) return;

            if (stamina instanceof StaminaOverride ov) {
                int combo = Math.max(0, payload.comboCount());
                int delay = 10 + (combo * 2);
                ov.addRegenDelay(delay);
            }

        });
    }
}