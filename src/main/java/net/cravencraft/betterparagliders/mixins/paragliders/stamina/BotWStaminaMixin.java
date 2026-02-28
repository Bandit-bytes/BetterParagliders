package net.cravencraft.betterparagliders.mixins.paragliders.stamina;

import net.cravencraft.betterparagliders.capabilities.StaminaOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tictim.paraglider.impl.stamina.BotWStamina;

@Mixin(BotWStamina.class)
public abstract class BotWStaminaMixin implements StaminaOverride {

    @Unique
    private int betterparagliders$regenDelayTicks = 0;

    @Override
    public int getRegenDelayTicks() {
        return betterparagliders$regenDelayTicks;
    }

    @Override
    public void setRegenDelayTicks(int ticks) {
        betterparagliders$regenDelayTicks = Math.max(0, ticks);
    }
}