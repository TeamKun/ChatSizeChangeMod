package net.kunmc.lab.chatsizechangemod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec.ConfigValue<Double> defaultChatSize;
    public static final ForgeConfigSpec config;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        defaultChatSize = builder.define("chatSizeChangeMod.defaultChatSize", 1.0);
        config = builder.build();
    }
}
