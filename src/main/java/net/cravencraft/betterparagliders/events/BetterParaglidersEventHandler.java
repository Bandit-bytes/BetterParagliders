package net.cravencraft.betterparagliders.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.cravencraft.betterparagliders.BetterParaglidersMod;
import net.cravencraft.betterparagliders.capabilities.StaminaOverride;
import net.cravencraft.betterparagliders.config.ConfigManager;
import net.cravencraft.betterparagliders.mixins.paragliders.accessors.PlayerMovementAccessor;
import net.cravencraft.betterparagliders.utils.CalculateStaminaUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.PlayerMovement;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = BetterParaglidersMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class BetterParaglidersEventHandler {

    private static final Map<ServerPlayer, MeleeActionState> MELEE_ACTIONS = new WeakHashMap<>();
    private static final int MELEE_HIT_GRACE_TICKS = 20;
    private static final int RANGED_DRAIN_INTERVAL_TICKS = 2;

    private BetterParaglidersEventHandler() {}

    private static Stamina getStamina(ServerPlayer player) {
        PlayerMovement movement = PlayerMovementProvider.of(player);
        if (movement instanceof PlayerMovementAccessor accessor) {
            Stamina stamina = accessor.betterparagliders$getStamina();
            if (stamina != null) return stamina;
        }
        return Stamina.get(player);
    }

    private static void drain(Stamina stamina, double amount) {
        if (amount > 0.0D) {
            stamina.takeStamina(amount, false, false, true, true, false);
        }
    }

    private static String registryKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static boolean isRangedItem(Item item) {
        return item instanceof BowItem
                || item instanceof CrossbowItem
                || CalculateStaminaUtils.DATAPACK_RANGED_STAMINA_OVERRIDES.containsKey(registryKey(item));
    }

    private static boolean isShieldItem(Item item) {
        return item instanceof ShieldItem
                || CalculateStaminaUtils.DATAPACK_SHIELD_STAMINA_OVERRIDES.containsKey(registryKey(item));
    }

    private static boolean isDepleted(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return false;
        Stamina stamina = getStamina(player);
        return stamina != null && stamina.isDepleted();
    }

    @SubscribeEvent
    public static void registerReloadListener(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) BetterParaglidersEventHandler::loadStaminaOverrides);
    }

    private static void loadStaminaOverrides(ResourceManager resourceManager) {
        CalculateStaminaUtils.clearDatapackStaminaOverrides();

        var found = resourceManager.listResourceStacks(
                "stamina_cost",
                fileName -> fileName.getPath().endsWith(".json")
        );

        BetterParaglidersMod.LOGGER.info("[BetterParagliders] Reloading stamina_cost JSONs: {}", found.keySet());

        for (Map.Entry<ResourceLocation, List<Resource>> entry : found.entrySet()) {
            String namespace = entry.getKey().getNamespace();

            for (Resource resource : entry.getValue()) {
                try (JsonReader reader = new JsonReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                    JsonElement root = JsonParser.parseReader(reader);
                    if (!root.isJsonArray()) {
                        BetterParaglidersMod.LOGGER.warn("[BetterParagliders] {} must contain a JSON array", entry.getKey());
                        continue;
                    }

                    JsonArray items = root.getAsJsonArray();
                    for (JsonElement element : items) {
                        if (!element.isJsonObject()) continue;
                        JsonObject object = element.getAsJsonObject();

                        if (!object.has("type") || !object.has("name") || !object.has("stamina_cost")) {
                            BetterParaglidersMod.LOGGER.warn("[BetterParagliders] Skipping incomplete stamina entry in {}: {}", entry.getKey(), object);
                            continue;
                        }

                        String type = object.get("type").getAsString();
                        String rawName = object.get("name").getAsString();
                        double cost = object.get("stamina_cost").getAsDouble();
                        String fullName = rawName.contains(":") ? rawName : namespace + ":" + rawName;
                        ResourceLocation itemId = ResourceLocation.tryParse(fullName);

                        if (itemId == null) {
                            BetterParaglidersMod.LOGGER.warn("[BetterParagliders] Invalid item id '{}' in {}", fullName, entry.getKey());
                            continue;
                        }

                        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                            BetterParaglidersMod.LOGGER.warn("[BetterParagliders] Unknown item id '{}' in {}", itemId, entry.getKey());
                            continue;
                        }

                        if (!CalculateStaminaUtils.addDatapackStaminaOverride(type, itemId.toString(), cost)) {
                            BetterParaglidersMod.LOGGER.warn("[BetterParagliders] Invalid stamina override in {}: type='{}', item='{}', cost={}", entry.getKey(), type, itemId, cost);
                            continue;
                        }

                        BetterParaglidersMod.LOGGER.info("[BetterParagliders] Loaded stamina override: {} {} -> {}", type, itemId, cost);
                    }
                } catch (Exception exception) {
                    BetterParaglidersMod.LOGGER.error("[BetterParagliders] Failed to read {}", entry.getKey(), exception);
                }
            }
        }
    }

    /** Called by the validated play-to-server packet when Better Combat starts a new combo step. */
    public static void handleMeleeSwingPacket(ServerPlayer player, int comboFromClient) {
        if (player.isCreative() || player.isSpectator() || comboFromClient <= 0) return;

        Stamina stamina = getStamina(player);
        if (stamina == null) return;

        int combo = Math.min(comboFromClient, 32);
        long now = player.level().getGameTime();
        MeleeActionState actionState = MELEE_ACTIONS.computeIfAbsent(player, ignored -> new MeleeActionState());

        if (actionState.lastActionTick == now && actionState.lastCombo == combo) {
            return;
        }

        actionState.lastActionTick = now;
        actionState.lastCombo = combo;

        if (stamina.isDepleted()) {
            actionState.paidUntil = Long.MIN_VALUE;
            return;
        }

        int cost = CalculateStaminaUtils.calculateMeleeStaminaCost(player, combo);
        if (cost > 0) {
            drain(stamina, cost);
            addDelay(stamina, ConfigManager.SERVER.meleeRegenDelayTicks());
        }

        // A paid attack is allowed to land even if paying its cost reduced stamina to zero.
        actionState.paidUntil = now + MELEE_HIT_GRACE_TICKS;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void shieldBlockEvent(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) return;
        if (event.getBlockedDamage() <= 0.0F || !player.isUsingItem()) return;
        if (!isShieldItem(player.getUseItem().getItem())) return;

        Stamina stamina = getStamina(player);
        if (stamina == null || stamina.isDepleted()) return;

        double projectileSurcharge = event.getSource().getDirectEntity() instanceof Projectile
                ? ConfigManager.SERVER.blockProjectileStaminaConsumption()
                : 0.0D;

        int cost = CalculateStaminaUtils.calculateBlockStaminaCost(
                player,
                event.getBlockedDamage(),
                projectileSurcharge
        );

        if (cost > 0) {
            drain(stamina, cost);
            addDelay(stamina, ConfigManager.SERVER.blockRegenDelayTicks());
        }

        if (stamina.isDepleted()) {
            player.stopUsingItem();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelAttackIfDepleted(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) return;
        if (!isDepleted(player)) return;

        MeleeActionState actionState = MELEE_ACTIONS.get(player);
        long paidUntil = actionState == null ? Long.MIN_VALUE : actionState.paidUntil;
        if (player.level().getGameTime() > paidUntil) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void rangedDrawDrain(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) return;

        Item item = event.getItem().getItem();
        if (!isRangedItem(item)) return;

        Stamina stamina = getStamina(player);
        if (stamina == null) return;

        if (stamina.isDepleted()) {
            if (!(item instanceof CrossbowItem) || !CrossbowItem.isCharged(event.getItem())) {
                player.stopUsingItem();
            }
            return;
        }

        double staminaPerSecond = CalculateStaminaUtils.calculateRangeStaminaPerSecond(player, registryKey(item));
        if (staminaPerSecond <= 0.0D) return;

        addDelay(stamina, Math.max(ConfigManager.SERVER.rangedRegenDelayTicks(), RANGED_DRAIN_INTERVAL_TICKS + 1));

        if ((player.tickCount % RANGED_DRAIN_INTERVAL_TICKS) == 0) {
            drain(stamina, staminaPerSecond * RANGED_DRAIN_INTERVAL_TICKS / 20.0D);

            if (stamina.isDepleted() && (!(item instanceof CrossbowItem) || !CrossbowItem.isCharged(event.getItem()))) {
                player.stopUsingItem();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRangedRelease(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator()) return;
        if (!isRangedItem(event.getItem().getItem())) return;

        Stamina stamina = getStamina(player);
        if (stamina != null) {
            addDelay(stamina, ConfigManager.SERVER.rangedRegenDelayTicks());
        }
    }

    @SubscribeEvent
    public static void cancelUseItemsRequiringStamina(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide() || !isDepleted(player)) return;

        Item item = event.getItem().getItem();
        if (isRangedItem(item)) {
            if (item instanceof CrossbowItem && CrossbowItem.isCharged(event.getItem())) return;
            player.stopUsingItem();
        } else if (isShieldItem(item)) {
            player.stopUsingItem();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventUnpaidMeleeDamage(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        DamageSource source = event.getSource();
        if (source.getDirectEntity() instanceof Projectile) return;

        Entity sourceEntity = source.getEntity();
        ServerPlayer player = sourceEntity instanceof ServerPlayer serverPlayer
                ? serverPlayer
                : source.getDirectEntity() instanceof ServerPlayer directPlayer ? directPlayer : null;

        if (player == null || player.isCreative() || player.isSpectator()) return;

        Stamina stamina = getStamina(player);
        if (stamina == null || !stamina.isDepleted()) return;

        MeleeActionState actionState = MELEE_ACTIONS.get(player);
        long paidUntil = actionState == null ? Long.MIN_VALUE : actionState.paidUntil;
        if (player.level().getGameTime() > paidUntil) {
            event.setNewDamage(0.0F);
        }
    }

    private static final class MeleeActionState {
        private long lastActionTick = Long.MIN_VALUE;
        private int lastCombo = Integer.MIN_VALUE;
        private long paidUntil = Long.MIN_VALUE;
    }

    private static void addDelay(Stamina stamina, int ticks) {
        if (stamina instanceof StaminaOverride override) {
            override.addRegenDelay(ticks);
        }
    }
}
