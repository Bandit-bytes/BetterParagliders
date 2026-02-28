package net.cravencraft.betterparagliders.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncActionToClientPayload(int totalActionStaminaCost) implements CustomPacketPayload {
    public static final Type<SyncActionToClientPayload> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath("betterparagliders", "sync_action_to_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncActionToClientPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncActionToClientPayload::totalActionStaminaCost,
                    SyncActionToClientPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}