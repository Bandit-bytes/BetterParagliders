package net.cravencraft.betterparagliders.utils;

import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.cravencraft.betterparagliders.attributes.BetterParaglidersAttributes;
import net.cravencraft.betterparagliders.config.ConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import tictim.paraglider.impl.movement.PlayerMovement;

import java.util.HashMap;
import java.util.Map;

public final class CalculateStaminaUtils {

    public static final Map<String, Double> DATAPACK_MELEE_STAMINA_OVERRIDES = new HashMap<>();
    public static final Map<String, Double> DATAPACK_RANGED_STAMINA_OVERRIDES = new HashMap<>();
    public static final Map<String, Double> DATAPACK_SHIELD_STAMINA_OVERRIDES = new HashMap<>();

    private CalculateStaminaUtils() {}

    public static void clearDatapackStaminaOverrides() {
        DATAPACK_MELEE_STAMINA_OVERRIDES.clear();
        DATAPACK_RANGED_STAMINA_OVERRIDES.clear();
        DATAPACK_SHIELD_STAMINA_OVERRIDES.clear();
    }

    public static boolean addDatapackStaminaOverride(String type, String itemId, double staminaCost) {
        if (!Double.isFinite(staminaCost) || staminaCost < 0.0D) return false;

        return switch (type) {
            case "shield" -> {
                DATAPACK_SHIELD_STAMINA_OVERRIDES.put(itemId, staminaCost);
                yield true;
            }
            case "ranged_weapon" -> {
                DATAPACK_RANGED_STAMINA_OVERRIDES.put(itemId, staminaCost);
                yield true;
            }
            case "melee_weapon" -> {
                DATAPACK_MELEE_STAMINA_OVERRIDES.put(itemId, staminaCost);
                yield true;
            }
            default -> false;
        };
    }

    private static String keyOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static int calculateMeleeStaminaCost(Player player, int currentCombo) {
        int safeCombo = Math.max(0, currentCombo);
        AttackHand attackHand = PlayerAttackHelper.getCurrentAttack(player, safeCombo);
        boolean hasHand = attackHand != null && !attackHand.itemStack().isEmpty();

        boolean isTwoHanded = hasHand && attackHand.attributes().isTwoHanded();
        String attackingItemId = hasHand ? keyOf(attackHand.itemStack().getItem()) : keyOf(player.getMainHandItem().getItem());

        double totalStaminaConsumption;
        if (DATAPACK_MELEE_STAMINA_OVERRIDES.containsKey(attackingItemId)) {
            totalStaminaConsumption = DATAPACK_MELEE_STAMINA_OVERRIDES.get(attackingItemId)
                    * ConfigManager.SERVER.meleeStaminaConsumption();
        } else {
            double baseMelee = 4.0D;
            double comboMultiplier = 1.0D + (safeCombo * 0.15D);
            double playerAttackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double damageBonus = Math.max(0.0D, playerAttackDamage - 1.0D) * 0.6D;

            totalStaminaConsumption = (baseMelee * comboMultiplier + damageBonus)
                    * ConfigManager.SERVER.meleeStaminaConsumption();
        }

        if (isTwoHanded) {
            totalStaminaConsumption = (totalStaminaConsumption * ConfigManager.SERVER.twoHandedStaminaConsumption())
                    - player.getAttributeValue(BetterParaglidersAttributes.TWO_HANDED_STAMINA_REDUCTION.getDelegate());
        } else {
            totalStaminaConsumption = (totalStaminaConsumption * ConfigManager.SERVER.oneHandedStaminaConsumption())
                    - player.getAttributeValue(BetterParaglidersAttributes.ONE_HANDED_STAMINA_REDUCTION.getDelegate());
        }

        totalStaminaConsumption -= player.getAttributeValue(
                BetterParaglidersAttributes.BASE_MELEE_STAMINA_REDUCTION.getDelegate()
        );

        return Math.max(0, (int) Math.ceil(totalStaminaConsumption));
    }

    public static double calculateRangeStaminaPerSecond(Player player, String itemIdKey) {
        double total = ConfigManager.SERVER.rangeStaminaConsumption();
        total += DATAPACK_RANGED_STAMINA_OVERRIDES.getOrDefault(itemIdKey, 0.0D);
        total -= player.getAttributeValue(BetterParaglidersAttributes.RANGE_STAMINA_REDUCTION.getDelegate());
        return Math.max(0.0D, total);
    }

    public static int calculateRangeStaminaCost(Player player, String itemIdKey) {
        return Math.max(0, (int) Math.ceil(calculateRangeStaminaPerSecond(player, itemIdKey)));
    }

    public static int calculateRangeStaminaCost(Player player) {
        return calculateRangeStaminaCost(player, keyOf(player.getUseItem().getItem()));
    }

    public static int calculateBlockStaminaCost(Player player, float blockedDamage) {
        return calculateBlockStaminaCost(player, blockedDamage, 0.0D);
    }

    public static int calculateBlockStaminaCost(Player player, float blockedDamage, double projectileSurcharge) {
        double total = ConfigManager.SERVER.blockStaminaConsumption()
                + Math.max(0.0F, blockedDamage)
                + Math.max(0.0D, projectileSurcharge);

        String shieldItemKey = keyOf(player.getUseItem().getItem());
        total += DATAPACK_SHIELD_STAMINA_OVERRIDES.getOrDefault(shieldItemKey, 0.0D);
        total -= player.getAttributeValue(BetterParaglidersAttributes.BLOCK_STAMINA_REDUCTION.getDelegate());

        return Math.max(0, (int) Math.ceil(total));
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

        if (original > 0.0D) return Math.max(modified, 0.0D);
        if (original < 0.0D) return Math.min(modified, 0.0D);
        return 0.0D;
    }
}
