package net.cravencraft.betterparagliders.mixins.paragliders.stamina;

import net.cravencraft.betterparagliders.capabilities.StaminaOverride;
import net.cravencraft.betterparagliders.utils.CalculateStaminaUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tictim.paraglider.api.movement.PlayerState;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.PlayerMovement;

@Mixin(PlayerMovement.class)
public abstract class PlayerMovementUpdateStaminaMixin {

    @Shadow public abstract Stamina stamina();
    @Shadow public abstract Player player();

    @Redirect(
            method = "updateStamina",
            at = @At(
                    value = "INVOKE",
                    target = "Ltictim/paraglider/impl/movement/PlayerMovement;staminaDelta()D"
            ),
            remap = false
    )
    private double betterparagliders$overrideDelta(PlayerMovement instance) {
        double staminaDelta = CalculateStaminaUtils.getModifiedStateChange(instance);

        Player p = this.player();
        if (p != null && p.isUsingItem()) {
            var item = p.getUseItem().getItem();
            String key = BuiltInRegistries.ITEM.getKey(item).toString();

            boolean ranged =
                    item instanceof BowItem
                            || item instanceof CrossbowItem
                            || CalculateStaminaUtils.DATAPACK_RANGED_STAMINA_OVERRIDES.containsKey(key);

            if (ranged && staminaDelta > 0.0D) {
                staminaDelta = 0.0D;
            }
        }

        Stamina s = this.stamina();
        if (s instanceof StaminaOverride ov) {
            int delay = ov.getRegenDelayTicks();
            if (delay > 0) {
                if (staminaDelta > 0.0D) staminaDelta = 0.0D;
                ov.setRegenDelayTicks(delay - 1);
            }
        }

        return staminaDelta;
    }
}