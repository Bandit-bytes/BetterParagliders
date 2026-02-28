package net.cravencraft.betterparagliders.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncActionToServerPayload(int comboCount) implements CustomPacketPayload {
    public static final Type<SyncActionToServerPayload> ID =
            new Type<>(ResourceLocation.fromNamespaceAndPath("betterparagliders", "sync_action_to_server"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncActionToServerPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SyncActionToServerPayload::comboCount,
                    SyncActionToServerPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}