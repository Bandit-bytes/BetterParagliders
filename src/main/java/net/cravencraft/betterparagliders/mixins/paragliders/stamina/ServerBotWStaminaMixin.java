package net.cravencraft.betterparagliders.mixins.paragliders.stamina;

import net.cravencraft.betterparagliders.capabilities.StaminaOverride;
import net.cravencraft.betterparagliders.events.PlayerMovementProvider;
import net.cravencraft.betterparagliders.mixins.paragliders.accessors.PlayerMovementAccessor;
import net.cravencraft.betterparagliders.network.ModNet;
import net.cravencraft.betterparagliders.network.SyncActionToClientPayload;
import net.cravencraft.betterparagliders.utils.CalculateStaminaUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.PlayerMovement;
import tictim.paraglider.impl.movement.ServerPlayerMovement;

@Mixin(ServerPlayerMovement.class)
public abstract class ServerBotWStaminaMixin {

    @Shadow public abstract ServerPlayer player();

    @Unique private boolean betterparagliders$wasUsingRanged = false;

    @Inject(method = "update()V", at = @At("HEAD"), remap = false, require = 1)
    private void betterparagliders$updateServerSide(CallbackInfo ci) {
        ServerPlayer p = this.player();
        if (p == null) return;

        boolean usingRanged = false;

        if (p.isUsingItem()) {
            var item = p.getUseItem().getItem();
            String key = BuiltInRegistries.ITEM.getKey(item).toString();

            usingRanged =
                    item instanceof BowItem
                            || item instanceof CrossbowItem
                            || CalculateStaminaUtils.DATAPACK_RANGED_STAMINA_OVERRIDES.containsKey(key);
        }

        if (usingRanged && !betterparagliders$wasUsingRanged) {
            int cost = Math.max(1, CalculateStaminaUtils.calculateRangeStaminaCost(p));
            betterparagliders$setPreviewCostAndSync(p, cost);
        }

        if (!usingRanged && betterparagliders$wasUsingRanged) {
            betterparagliders$setPreviewCostAndSync(p, 0);
        }

        betterparagliders$wasUsingRanged = usingRanged;
    }

    @Unique
    private void betterparagliders$setPreviewCostAndSync(ServerPlayer p, int previewCost) {
        Stamina s = betterparagliders$getMovementStamina(p);
        if (s instanceof StaminaOverride override) {
            override.setTotalActionStaminaCost(previewCost);
        }
        ModNet.sendToPlayer(p, new SyncActionToClientPayload(previewCost));
    }

    @Unique
    private static Stamina betterparagliders$getMovementStamina(ServerPlayer p) {
        PlayerMovement movement = PlayerMovementProvider.of(p);
        if (movement instanceof PlayerMovementAccessor accessor) {
            Stamina s = accessor.betterparagliders$getStamina();
            if (s != null) return s;
        }
        return Stamina.get(p);
    }
}