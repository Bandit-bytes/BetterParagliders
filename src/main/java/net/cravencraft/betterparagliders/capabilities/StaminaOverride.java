package net.cravencraft.betterparagliders.capabilities;

public interface StaminaOverride {

    int getRegenDelayTicks();
    void setRegenDelayTicks(int ticks);

    default void addRegenDelay(int ticks) {
        setRegenDelayTicks(Math.max(getRegenDelayTicks(), ticks));
    }

    default int getTotalActionStaminaCost() { return 0; }
    default void setTotalActionStaminaCost(int totalActionStaminaCost) {}
}