package net.cravencraft.betterparagliders.mixins.paragliders.accessors;

import net.cravencraft.betterparagliders.accessors.PlayerMovementStaminaAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.PlayerMovement;

@Mixin(PlayerMovement.class)
public abstract class PlayerMovementStaminaAccessMixin implements PlayerMovementStaminaAccess {

    @Shadow
    public abstract Stamina stamina();

    @Override
    public Stamina betterparagliders$getMovementStamina() {
        return this.stamina();
    }
}
