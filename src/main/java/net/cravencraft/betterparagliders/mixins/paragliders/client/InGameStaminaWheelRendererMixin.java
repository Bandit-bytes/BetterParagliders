package net.cravencraft.betterparagliders.mixins.paragliders.client;

import net.cravencraft.betterparagliders.utils.CalculateStaminaUtils;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.client.render.InGameStaminaWheelRenderer;
import tictim.paraglider.impl.movement.PlayerMovement;

@Mixin(InGameStaminaWheelRenderer.class)
public abstract class InGameStaminaWheelRendererMixin {

    @Redirect(
            method = "makeWheel(Lnet/minecraft/world/entity/player/Player;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ltictim/paraglider/api/movement/Movement;staminaDelta()D"
            ),
            remap = false
    )
    private double betterparagliders$overrideStaminaDelta(Movement movement) {
        if (!(movement instanceof PlayerMovement pm)) {
            return 0.0;
        }

        Player player = pm.player();

        double staminaDelta = CalculateStaminaUtils.getModifiedStateChange(pm);
        if (CalculateStaminaUtils.getAdditionalMovementStaminaCost(pm.state().id().getPath())) {
            staminaDelta = 0.0;
        }

        return staminaDelta;
    }
}