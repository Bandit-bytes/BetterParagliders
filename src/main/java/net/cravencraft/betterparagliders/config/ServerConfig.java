package net.cravencraft.betterparagliders.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {

    private final ModConfigSpec.DoubleValue MELEE_STAMINA_CONSUMPTION;
    private final ModConfigSpec.DoubleValue TWO_HANDED_STAMINA_CONSUMPTION;
    private final ModConfigSpec.DoubleValue ONE_HANDED_STAMINA_CONSUMPTION;
    private final ModConfigSpec.DoubleValue RANGE_STAMINA_CONSUMPTION;
    private final ModConfigSpec.DoubleValue BLOCK_STAMINA_CONSUMPTION;
    private final ModConfigSpec.DoubleValue BLOCK_PROJECTILE_STAMINA_CONSUMPTION;
    private final ModConfigSpec.IntValue MELEE_REGEN_DELAY_TICKS;
    private final ModConfigSpec.IntValue RANGED_REGEN_DELAY_TICKS;
    private final ModConfigSpec.IntValue BLOCK_REGEN_DELAY_TICKS;

    public final ModConfigSpec spec;

    public ServerConfig(ModConfigSpec.Builder server) {
        server.push("stamina");

        MELEE_STAMINA_CONSUMPTION = server
                .comment("Multiplier applied to the base stamina cost of every Better Combat swing. Set to 0 to disable melee stamina costs.")
                .defineInRange("melee_stamina_consumption", 1.0, 0.0, 10.0);

        TWO_HANDED_STAMINA_CONSUMPTION = server
                .comment("Additional multiplier for two-handed attacks. Stacks with melee_stamina_consumption.")
                .defineInRange("two_handed_stamina_consumption", 1.5, 0.0, 10.0);

        ONE_HANDED_STAMINA_CONSUMPTION = server
                .comment("Additional multiplier for one-handed and dual-wield attacks. Stacks with melee_stamina_consumption.")
                .defineInRange("one_handed_stamina_consumption", 1.0, 0.0, 10.0);

        RANGE_STAMINA_CONSUMPTION = server
                .comment("Base stamina drained per second while drawing/charging a ranged weapon. Datapack values are added to this. Set to 0 to disable the base drain.")
                .defineInRange("ranged_stamina_consumption", 5.0, 0.0, 100.0);

        BLOCK_STAMINA_CONSUMPTION = server
                .comment("Base stamina cost of a successful shield block. Blocked damage and datapack values are added to this.")
                .defineInRange("block_stamina_consumption", 10.0, 0.0, 100.0);

        BLOCK_PROJECTILE_STAMINA_CONSUMPTION = server
                .comment("Additional stamina cost for a successful projectile block. Charged only once, together with the normal block cost.")
                .defineInRange("block_projectile_stamina_consumption", 5.0, 0.0, 100.0);

        MELEE_REGEN_DELAY_TICKS = server
                .comment("Ticks before stamina can begin regenerating after a melee swing. 20 ticks = 1 second.")
                .defineInRange("melee_regen_delay_ticks", 40, 0, 400);

        RANGED_REGEN_DELAY_TICKS = server
                .comment("Ticks before stamina can begin regenerating after releasing a ranged weapon.")
                .defineInRange("ranged_regen_delay_ticks", 10, 0, 400);

        BLOCK_REGEN_DELAY_TICKS = server
                .comment("Ticks before stamina can begin regenerating after a successful shield block.")
                .defineInRange("block_regen_delay_ticks", 10, 0, 400);

        server.pop();
        this.spec = server.build();
    }

    public double meleeStaminaConsumption() { return MELEE_STAMINA_CONSUMPTION.get(); }
    public double twoHandedStaminaConsumption() { return TWO_HANDED_STAMINA_CONSUMPTION.get(); }
    public double oneHandedStaminaConsumption() { return ONE_HANDED_STAMINA_CONSUMPTION.get(); }
    public double rangeStaminaConsumption() { return RANGE_STAMINA_CONSUMPTION.get(); }
    public double blockStaminaConsumption() { return BLOCK_STAMINA_CONSUMPTION.get(); }
    public double blockProjectileStaminaConsumption() { return BLOCK_PROJECTILE_STAMINA_CONSUMPTION.get(); }
    public int meleeRegenDelayTicks() { return MELEE_REGEN_DELAY_TICKS.get(); }
    public int rangedRegenDelayTicks() { return RANGED_REGEN_DELAY_TICKS.get(); }
    public int blockRegenDelayTicks() { return BLOCK_REGEN_DELAY_TICKS.get(); }
}
