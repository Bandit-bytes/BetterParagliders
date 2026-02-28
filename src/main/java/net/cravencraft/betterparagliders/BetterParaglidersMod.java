package net.cravencraft.betterparagliders;

import net.cravencraft.betterparagliders.attributes.BetterParaglidersAttributes;
import net.cravencraft.betterparagliders.config.ConfigManager;
import net.cravencraft.betterparagliders.network.ModNet;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BetterParaglidersMod.MOD_ID)
public class BetterParaglidersMod {
    public static final Logger LOGGER = LogManager.getLogger("BetterParaglidersMod");
    public static final String MOD_ID = "betterparagliders";

    public BetterParaglidersMod(IEventBus modBus, ModContainer container) {
        BetterParaglidersAttributes.register(modBus);
        ModNet.init(modBus);
        ConfigManager.register(container);
        modBus.addListener(BetterParaglidersMod::onEntityAttributeModification);
    }

    private static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        // Mobility
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.SPRINTING_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.SWIMMING_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.IDLE_STAMINA_REGEN);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.SUBMERGED_STAMINA_REGEN);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.WATER_BREATHING_STAMINA_REGEN);

        // ParCool Mobility
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.FAST_RUNNING_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.FAST_SWIMMING_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.CLING_TO_CLIFF_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.HORIZONTAL_WALL_RUN_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.DODGE_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.ROLL_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.BREAKFALL_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.VAULT_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.CLIMB_UP_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.VERTICAL_WALL_RUN_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.CAT_LEAP_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.CHARGE_JUMP_STAMINA_REDUCTION);

        // Combat
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.BASE_MELEE_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.ONE_HANDED_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.TWO_HANDED_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.RANGE_STAMINA_REDUCTION);
        event.add(EntityType.PLAYER, BetterParaglidersAttributes.BLOCK_STAMINA_REDUCTION);
    }
}