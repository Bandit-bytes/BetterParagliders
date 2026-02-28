package net.cravencraft.betterparagliders.mixins.paragliders.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.PlayerMovement;

@Mixin(PlayerMovement.class)
public interface PlayerMovementAccessor {
    @Accessor("stamina")
    Stamina betterparagliders$getStamina();
}