package net.cravencraft.betterparagliders.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.bettercombat.logic.PlayerAttackProperties;
import net.cravencraft.betterparagliders.BetterParaglidersMod;
import net.cravencraft.betterparagliders.config.ConfigManager;
import net.cravencraft.betterparagliders.mixins.paragliders.accessors.PlayerMovementAccessor;
import net.cravencraft.betterparagliders.utils.CalculateStaminaUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.PlayerMovement;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = BetterParaglidersMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class BetterParaglidersEventHandler {

    private BetterParaglidersEventHandler() {}

    private static Stamina getStamina(ServerPlayer player) {
        PlayerMovement movement = PlayerMovementProvider.of(player);
        if (movement instanceof PlayerMovementAccessor accessor) {
            Stamina s = accessor.betterparagliders$getStamina();
            if (s != null) return s;
        }
        return Stamina.get(player);
    }

    private static double drain(Stamina stamina, double amount) {
        return stamina.takeStamina(amount, false, false, true, true, false);
    }

    private static String normalizeToRegistryId(String namespace, String rawName) {
        if (rawName.contains(":")) return rawName;
        return namespace + ":" + rawName;
    }

    private static String registryKey(net.minecraft.world.item.Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString(); // "minecraft:bow"
    }

    private static boolean isDepleted(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return false;
        Stamina stamina = getStamina(player);
        return stamina != null && stamina.isDepleted();
    }

    @SubscribeEvent
    public static void loadStaminaOverrides(ServerStartedEvent event) {
        ResourceManager resourceManager = event.getServer().getResourceManager();

        var found = resourceManager.listResourceStacks(
                "stamina_cost",
                (fileName) -> fileName.getPath().endsWith(".json")
        );

        BetterParaglidersMod.LOGGER.info("[BetterParagliders] Found stamina_cost jsons: {}", found.keySet());

        for (Map.Entry<ResourceLocation, List<Resource>> entry : found.entrySet()) {
            String namespace = entry.getKey().getNamespace();

            for (Resource resource : entry.getValue()) {
                try (JsonReader reader = new JsonReader(new InputStreamReader(resource.open()))) {
                    JsonArray items = JsonParser.parseReader(reader).getAsJsonArray();

                    for (JsonElement el : items) {
                        if (!el.isJsonObject()) continue;
                        JsonObject obj = el.getAsJsonObject();

                        if (!obj.has("type") || !obj.has("name") || !obj.has("stamina_cost")) continue;

                        String type = obj.get("type").getAsString();
                        String rawName = obj.get("name").getAsString();
                        double cost = obj.get("stamina_cost").getAsDouble();

                        String key = normalizeToRegistryId(namespace, rawName);
                        CalculateStaminaUtils.addDatapackStaminaOverride(type, key, cost);

                        BetterParaglidersMod.LOGGER.info("[BetterParagliders] Loaded stamina override: {} {} -> {}", type, key, cost);
                    }
                } catch (Exception e) {
                    BetterParaglidersMod.LOGGER.error("[BetterParagliders] Bad JSON in {}", entry.getKey(), e);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void shieldBlockEvent(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.isCreative() || player.isSpectator()) return;

        drainShieldBlock(player, event.getBlockedDamage());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void shieldBlockProjectileEvent(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.isCreative() || player.isSpectator()) return;

        drainShieldBlock(player, (float) ConfigManager.SERVER.blockProjectileStaminaConsumption());
    }

    private static void drainShieldBlock(ServerPlayer player, float blockedAmount) {
        if (!player.isUsingItem()) return;
        if (!(player.getUseItem().getItem() instanceof ShieldItem)) return;

        Stamina stamina = getStamina(player);
        if (stamina == null || stamina.isDepleted()) return;

        int cost = Math.max(1, CalculateStaminaUtils.calculateBlockStaminaCost(player, blockedAmount));
        drain(stamina, cost);

        addDelay(stamina, 10);
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelAttackIfDepleted(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.isCreative() || player.isSpectator()) return;

        Stamina stamina = getStamina(player);
        if (stamina != null && stamina.isDepleted()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void bowDrawDrain(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.isCreative() || player.isSpectator()) return;

        var stack = event.getItem();
        var item = stack.getItem();

        boolean bowLike = item instanceof BowItem || item instanceof CrossbowItem;
        String key = registryKey(item);
        boolean overridden = CalculateStaminaUtils.DATAPACK_RANGED_STAMINA_OVERRIDES.containsKey(key);
        if (!bowLike && !overridden) return;

        Stamina stamina = getStamina(player);
        if (stamina == null) return;

        if (stamina.isDepleted()) {
            player.stopUsingItem();
            return;
        }

        double perSecond = ConfigManager.SERVER.rangeStaminaConsumption();

        if (overridden) {
            perSecond += CalculateStaminaUtils.DATAPACK_RANGED_STAMINA_OVERRIDES.get(key);
        }

        perSecond -= player.getAttributeValue(
                net.cravencraft.betterparagliders.attributes.BetterParaglidersAttributes
                        .RANGE_STAMINA_REDUCTION.getDelegate()
        );

        if (perSecond < 0.0) perSecond = 0.0;

        final double HOLD_SCALE = 10.0;
        perSecond *= HOLD_SCALE;

        final int intervalTicks = 2;

        addDelay(stamina, intervalTicks + 2);

        if ((player.tickCount % intervalTicks) == 0) {
            double amount = (perSecond / 20.0) * intervalTicks;

            if (amount > 0.0) {
                drain(stamina, amount);
            }

            addDelay(stamina, intervalTicks + 4);
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRangedRelease(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.isCreative() || player.isSpectator()) return;

        var stack = event.getItem();
        var item = stack.getItem();

        boolean bowLike = item instanceof BowItem || item instanceof CrossbowItem;
        String key = registryKey(item);
        boolean overridden = CalculateStaminaUtils.DATAPACK_RANGED_STAMINA_OVERRIDES.containsKey(key);
        if (!bowLike && !overridden) return;

        int timeUsed = stack.getUseDuration(player) - event.getDuration();
        if (timeUsed < 5) return;

        Stamina stamina = getStamina(player);
        if (stamina == null) return;

        addDelay(stamina, 10);
    }
    @SubscribeEvent
    public static void cancelUseItemsRequiringStamina(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!isDepleted(player)) return;

        var item = event.getItem().getItem();
        String key = registryKey(item);

        boolean ranged = item instanceof BowItem
                || item instanceof CrossbowItem
                || CalculateStaminaUtils.DATAPACK_RANGED_STAMINA_OVERRIDES.containsKey(key);

        boolean shield = item instanceof ShieldItem
                || CalculateStaminaUtils.DATAPACK_SHIELD_STAMINA_OVERRIDES.containsKey(key);

        if (ranged) {
            if (item instanceof CrossbowItem && CrossbowItem.isCharged(event.getItem())) return;
            player.stopUsingItem();
        } else if (shield) {
            player.stopUsingItem();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drainOnMeleeDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (event.getSource().getDirectEntity() instanceof Projectile) return;

        Stamina stamina = getStamina(player);
        if (stamina == null) return;

        if (stamina.isDepleted()) {
            event.setCanceled(true);
            return;
        }

        int combo = (player instanceof PlayerAttackProperties props) ? props.getComboCount() : 0;

        int cost = Math.max(1, CalculateStaminaUtils.calculateMeleeStaminaCost(player, combo));
        drain(stamina, cost);
    }
    private static void addDelay(Stamina stamina, int ticks) {
        if (stamina instanceof net.cravencraft.betterparagliders.capabilities.StaminaOverride ov) {
            ov.addRegenDelay(ticks);
        }
    }
}