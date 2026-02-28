package net.cravencraft.betterparagliders.mixins.paragliders.capabilities;

import net.bettercombat.api.MinecraftClient_BetterCombat;
import net.bettercombat.logic.PlayerAttackProperties;
import net.cravencraft.betterparagliders.network.SyncActionToServerPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.ClientPlayerMovement;

@Mixin(ClientPlayerMovement.class)
public abstract class ClientPlayerMovementMixin {

    @Shadow public abstract LocalPlayer player();

    @Unique
    private int betterparagliders$lastComboSent = 0;

    @Inject(method = "update()V", at = @At("HEAD"), remap = false, require = 1)
    private void betterparagliders$update(CallbackInfo ci) {
        LocalPlayer p = this.player();
        if (p == null) return;

        Stamina stamina = Stamina.get(p);
        if (!p.isCreative() && !p.isSpectator() && stamina.isDepleted()) {
            ((MinecraftClient_BetterCombat) Minecraft.getInstance()).cancelUpswing();
            return;
        }

        betterparagliders$sendComboIfChanged(p);
    }

    @Unique
    private void betterparagliders$sendComboIfChanged(LocalPlayer p) {
        int currentCombo = ((PlayerAttackProperties) p).getComboCount();

        if (currentCombo == 0) {
            betterparagliders$lastComboSent = 0;
            return;
        }

        if (currentCombo != betterparagliders$lastComboSent) {
            betterparagliders$lastComboSent = currentCombo;
            PacketDistributor.sendToServer(new SyncActionToServerPayload(currentCombo));
        }
    }
}