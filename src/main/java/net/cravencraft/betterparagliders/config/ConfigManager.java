package net.cravencraft.betterparagliders.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigManager {

    public static ClientConfig CLIENT;
    public static ServerConfig SERVER;

    public static void register(ModContainer container) {
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();

        CLIENT = new ClientConfig(clientBuilder);
        SERVER = new ServerConfig(serverBuilder);

        container.registerConfig(ModConfig.Type.CLIENT, CLIENT.spec);
        container.registerConfig(ModConfig.Type.SERVER, SERVER.spec);
    }
}