package net.cravencraft.betterparagliders.events;

import net.cravencraft.betterparagliders.accessors.PlayerMovementStaminaAccess;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.PlayerMovement;

import java.lang.reflect.Method;

public final class PlayerMovementProvider {
    private PlayerMovementProvider() {}

    @Nullable
    public static Stamina stamina(Player player) {
        PlayerMovement movement = of(player);
        if (movement instanceof PlayerMovementStaminaAccess acc) {
            return acc.betterparagliders$getMovementStamina();
        }
        return Stamina.get(player);
    }

    @Nullable
    public static PlayerMovement of(Player player) {
        Object map = ParagliderMod.instance().getPlayerConnectionMap();

        PlayerMovement movement = tryInvoke(map, "get", player);
        if (movement != null) return movement;

        movement = tryInvoke(map, "getMovement", player);
        if (movement != null) return movement;

        movement = tryInvoke(map, "movementOf", player);
        if (movement != null) return movement;

        movement = tryInvoke(map, "getOrCreate", player);
        return movement;
    }

    @Nullable
    private static PlayerMovement tryInvoke(Object target, String methodName, Player player) {
        try {
            Method m = target.getClass().getMethod(methodName, Player.class);
            Object result = m.invoke(target, player);
            return (result instanceof PlayerMovement pm) ? pm : null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}