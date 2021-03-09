package net.kunmc.lab.chatsizechangemod.config;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigChangePacketContainer {
    private final double defaultChatSize;
    private final double minChatSize;
    private final double maxChatSize;
    private final double chatSizeMultiply;
    private final double chatBaseSize;

    public ConfigChangePacketContainer(double defaultChatSize, double minChatSize, double maxChatSize, double chatSizeMultiply, double chatBaseSize) {
        this.defaultChatSize = defaultChatSize;
        this.minChatSize = minChatSize;
        this.maxChatSize = maxChatSize;
        this.chatSizeMultiply = chatSizeMultiply;
        this.chatBaseSize = chatBaseSize;
    }

    public static void encode(ConfigChangePacketContainer message, PacketBuffer buffer) {
        buffer.writeDouble(message.defaultChatSize);
        buffer.writeDouble(message.minChatSize);
        buffer.writeDouble(message.maxChatSize);
        buffer.writeDouble(message.chatSizeMultiply);
        buffer.writeDouble(message.chatBaseSize);
    }

    public static ConfigChangePacketContainer decode(PacketBuffer buffer) {
        double defaultChatSize = buffer.readDouble();
        double minChatSize = buffer.readDouble();
        double maxChatSize = buffer.readDouble();
        double chatSizeMultiply = buffer.readDouble();
        double chatBaseSize = buffer.readDouble();
        return new ConfigChangePacketContainer(defaultChatSize, minChatSize, maxChatSize, chatSizeMultiply, chatBaseSize);
    }

    public static boolean handle(ConfigChangePacketContainer message, Supplier<NetworkEvent.Context> supplier) {
        ChatSizeChangeModConfig.defaultChatSize = message.defaultChatSize;
        ChatSizeChangeModConfig.minChatSize = message.minChatSize;
        ChatSizeChangeModConfig.maxChatSize = message.maxChatSize;
        ChatSizeChangeModConfig.chatSizeMultiply = message.chatSizeMultiply;
        ChatSizeChangeModConfig.chatBaseSize = message.chatBaseSize;
        return true;
    }
}
