package net.cravencraft.betterparagliders.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public final ModConfigSpec spec;

    public ClientConfig(ModConfigSpec.Builder client) {
        client.push("gui");


        client.pop();
        this.spec = client.build();
    }
}