package net.cravencraft.betterparagliders.attributes;

import net.cravencraft.betterparagliders.BetterParaglidersMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.cravencraft.betterparagliders.BetterParaglidersMod.MOD_ID;

public class BetterParaglidersAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, MOD_ID);

    /**
     * Mobility Attributes
     */
    public static final DeferredHolder<Attribute, Attribute> SPRINTING_STAMINA_REDUCTION =
            ATTRIBUTES.register("sprinting_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".sprinting_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> SWIMMING_STAMINA_REDUCTION =
            ATTRIBUTES.register("swimming_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".swimming_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> IDLE_STAMINA_REGEN =
            ATTRIBUTES.register("idle_stamina_regen",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".idle_stamina_regen",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> SUBMERGED_STAMINA_REGEN =
            ATTRIBUTES.register("submerged_stamina_regen",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".submerged_stamina_regen",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> WATER_BREATHING_STAMINA_REGEN =
            ATTRIBUTES.register("water_breathing_stamina_regen",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".water_breathing_stamina_regen",
                            0.0, 0.0, 100.0
                    )
            );

    /**
     * ParCool Support. Mobility Attributes
     */
    public static final DeferredHolder<Attribute, Attribute> FAST_RUNNING_STAMINA_REDUCTION =
            ATTRIBUTES.register("fast_running_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".fast_running_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> FAST_SWIMMING_STAMINA_REDUCTION =
            ATTRIBUTES.register("fast_swimming_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".fast_swimming_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> CLING_TO_CLIFF_STAMINA_REDUCTION =
            ATTRIBUTES.register("cling_to_cliff_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".cling_to_cliff_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> HORIZONTAL_WALL_RUN_STAMINA_REDUCTION =
            ATTRIBUTES.register("horizontal_wall_run_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".horizontal_wall_run_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> DODGE_STAMINA_REDUCTION =
            ATTRIBUTES.register("dodge_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".dodge_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> ROLL_STAMINA_REDUCTION =
            ATTRIBUTES.register("roll_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".roll_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> BREAKFALL_STAMINA_REDUCTION =
            ATTRIBUTES.register("breakfall_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".breakfall_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> VAULT_STAMINA_REDUCTION =
            ATTRIBUTES.register("vault_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".vault_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> CLIMB_UP_STAMINA_REDUCTION =
            ATTRIBUTES.register("climb_up_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".climb_up_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> VERTICAL_WALL_RUN_STAMINA_REDUCTION =
            ATTRIBUTES.register("vertical_wall_run_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".vertical_wall_run_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> CAT_LEAP_STAMINA_REDUCTION =
            ATTRIBUTES.register("cat_leap_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".cat_leap_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> CHARGE_JUMP_STAMINA_REDUCTION =
            ATTRIBUTES.register("charge_jump_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".charge_jump_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    /**
     * Combat Attributes
     */
    public static final DeferredHolder<Attribute, Attribute> BASE_MELEE_STAMINA_REDUCTION =
            ATTRIBUTES.register("base_melee_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".base_melee_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> TWO_HANDED_STAMINA_REDUCTION =
            ATTRIBUTES.register("two_handed_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".two_handed_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> ONE_HANDED_STAMINA_REDUCTION =
            ATTRIBUTES.register("one_handed_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".one_handed_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> RANGE_STAMINA_REDUCTION =
            ATTRIBUTES.register("range_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".range_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static final DeferredHolder<Attribute, Attribute> BLOCK_STAMINA_REDUCTION =
            ATTRIBUTES.register("block_stamina_reduction",
                    () -> new RangedAttribute(
                            "attribute.name." + BetterParaglidersMod.MOD_ID + ".block_stamina_reduction",
                            0.0, 0.0, 100.0
                    )
            );

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }
}