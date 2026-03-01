package net.cravencraft.betterparagliders.utils;

import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.cravencraft.betterparagliders.attributes.BetterParaglidersAttributes;
import net.cravencraft.betterparagliders.config.ConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import tictim.paraglider.impl.movement.PlayerMovement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateStaminaUtils {

    public static Map<String, Double> DATAPACK_MELEE_STAMINA_OVERRIDES = new HashMap<>();
    public static Map<String, Double> DATAPACK_RANGED_STAMINA_OVERRIDES = new HashMap<>();
    public static Map<String, Double> DATAPACK_SHIELD_STAMINA_OVERRIDES = new HashMap<>();

    public static final List<String> ADDITIONAL_STAMINA_COST_MOVEMENT_STATES = List.of(
            "dodge", "breakfall", "roll", "vault", "climb_up", "cling_to_cliff",
            "vertical_wall_run", "cat_leap", "charge_jump"
    );

    public static void addDatapackStaminaOverride(String type, String itemId, double staminaCost) {
        switch (type) {
            case "shield" -> DATAPACK_SHIELD_STAMINA_OVERRIDES.put(itemId, staminaCost);
            case "ranged_weapon" -> DATAPACK_RANGED_STAMINA_OVERRIDES.put(itemId, staminaCost);
            case "melee_weapon" -> DATAPACK_MELEE_STAMINA_OVERRIDES.put(itemId, staminaCost);
        }
    }

    private static String keyOf(net.minecraft.world.item.Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static int calculateMeleeStaminaCost(Player player, int currentCombo) {
        AttackHand attackHand = PlayerAttackHelper.getCurrentAttack(player, currentCombo);
        boolean hasHand = attackHand != null && !attackHand.itemStack().isEmpty();

        boolean isTwoHanded = hasHand && attackHand.attributes().isTwoHanded();
        String attackingItemId = hasHand ? keyOf(attackHand.itemStack().getItem()) : "minecraft:air";

        double totalStaminaConsumption;
        if (hasHand && DATAPACK_MELEE_STAMINA_OVERRIDES.containsKey(attackingItemId)) {
            totalStaminaConsumption =
                    DATAPACK_MELEE_STAMINA_OVERRIDES.get(attackingItemId)
                            * ConfigManager.SERVER.meleeStaminaConsumption();
        } else {
            double baseMelee = 4.0;
            double comboMultiplier = 1.0 + (currentCombo * 0.15);
            double playerAttackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double damageBonus = Math.max(0.0, playerAttackDamage - 1.0) * 0.6;

            totalStaminaConsumption =
                    (baseMelee * comboMultiplier + damageBonus)
                            * ConfigManager.SERVER.meleeStaminaConsumption();
        }

        if (isTwoHanded) {
            totalStaminaConsumption =
                    (totalStaminaConsumption * ConfigManager.SERVER.twoHandedStaminaConsumption())
                            - player.getAttributeValue(BetterParaglidersAttributes.TWO_HANDED_STAMINA_REDUCTION.getDelegate());
        } else {
            totalStaminaConsumption =
                    (totalStaminaConsumption * ConfigManager.SERVER.oneHandedStaminaConsumption())
                            - player.getAttributeValue(BetterParaglidersAttributes.ONE_HANDED_STAMINA_REDUCTION.getDelegate());
        }

        totalStaminaConsumption -=
                player.getAttributeValue(BetterParaglidersAttributes.BASE_MELEE_STAMINA_REDUCTION.getDelegate());

        int cost = (int) Math.ceil(totalStaminaConsumption);
        return Math.max(1, cost);
    }

    public static int calculateRangeStaminaCost(Player player, String itemIdKey) {
        double total = ConfigManager.SERVER.rangeStaminaConsumption();

        if (DATAPACK_RANGED_STAMINA_OVERRIDES.containsKey(itemIdKey)) {
            total += DATAPACK_RANGED_STAMINA_OVERRIDES.get(itemIdKey);
        }

        int cost = (int) Math.ceil(total - player.getAttributeValue(BetterParaglidersAttributes.RANGE_STAMINA_REDUCTION.getDelegate()));
        return Math.max(1, cost);
    }

    public static int calculateRangeStaminaCost(Player player) {
        String bowItemKey = keyOf(player.getUseItem().getItem());
        return calculateRangeStaminaCost(player, bowItemKey);
    }

    public static int calculateBlockStaminaCost(Player player, float blockedDamage) {
        double total = ConfigManager.SERVER.blockStaminaConsumption() + blockedDamage;

        String shieldItemKey = keyOf(player.getUseItem().getItem());

        if (DATAPACK_SHIELD_STAMINA_OVERRIDES.containsKey(shieldItemKey)) {
            total += DATAPACK_SHIELD_STAMINA_OVERRIDES.get(shieldItemKey);
        }

        total -= player.getAttributeValue(BetterParaglidersAttributes.BLOCK_STAMINA_REDUCTION.getDelegate());
        int cost = (int) Math.ceil(total);
        return Math.max(1, cost);
    }

    public static double getModifiedStateChange(PlayerMovement playerMovement) {
        double original = playerMovement.staminaDelta();

        Player player = playerMovement.player();
        String state = playerMovement.state().id().getPath();

        double modified = switch (state) {
            case "idle" -> original + player.getAttributeValue(BetterParaglidersAttributes.IDLE_STAMINA_REGEN.getDelegate());
            case "running" -> original + player.getAttributeValue(BetterParaglidersAttributes.SPRINTING_STAMINA_REDUCTION.getDelegate());
            case "swimming" -> original + player.getAttributeValue(BetterParaglidersAttributes.SWIMMING_STAMINA_REDUCTION.getDelegate());
            case "underwater" -> original + player.getAttributeValue(BetterParaglidersAttributes.SUBMERGED_STAMINA_REGEN.getDelegate());
            case "breathing_underwater" -> original + player.getAttributeValue(BetterParaglidersAttributes.WATER_BREATHING_STAMINA_REGEN.getDelegate());

            case "fast_running" -> original + player.getAttributeValue(BetterParaglidersAttributes.FAST_RUNNING_STAMINA_REDUCTION.getDelegate());
            case "fast_swimming" -> original + player.getAttributeValue(BetterParaglidersAttributes.FAST_SWIMMING_STAMINA_REDUCTION.getDelegate());
            case "horizontal_wall_run" -> original + player.getAttributeValue(BetterParaglidersAttributes.HORIZONTAL_WALL_RUN_STAMINA_REDUCTION.getDelegate());
            case "cling_to_cliff" -> original + player.getAttributeValue(BetterParaglidersAttributes.CLING_TO_CLIFF_STAMINA_REDUCTION.getDelegate());
            case "dodge" -> original + player.getAttributeValue(BetterParaglidersAttributes.DODGE_STAMINA_REDUCTION.getDelegate());
            case "roll" -> original + player.getAttributeValue(BetterParaglidersAttributes.ROLL_STAMINA_REDUCTION.getDelegate());
            case "climb_up" -> original + player.getAttributeValue(BetterParaglidersAttributes.CLIMB_UP_STAMINA_REDUCTION.getDelegate());
            case "breakfall" -> original + player.getAttributeValue(BetterParaglidersAttributes.BREAKFALL_STAMINA_REDUCTION.getDelegate());
            case "vault" -> original + player.getAttributeValue(BetterParaglidersAttributes.VAULT_STAMINA_REDUCTION.getDelegate());
            case "vertical_wall_run" -> original + player.getAttributeValue(BetterParaglidersAttributes.VERTICAL_WALL_RUN_STAMINA_REDUCTION.getDelegate());
            case "cat_leap" -> original + player.getAttributeValue(BetterParaglidersAttributes.CAT_LEAP_STAMINA_REDUCTION.getDelegate());
            case "charge_jump" -> original + player.getAttributeValue(BetterParaglidersAttributes.CHARGE_JUMP_STAMINA_REDUCTION.getDelegate());

            default -> original;
        };

        if (original >= 0.0) {
            return Math.max(modified, original);
        } else {
            return Math.min(0.0, modified);
        }
    }

    public static boolean getAdditionalMovementStaminaCost(String playerState) {
        return ADDITIONAL_STAMINA_COST_MOVEMENT_STATES.contains(playerState);
    }
}