package net.kunmc.lab.chatsizechangemod;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketContainer {
    private final Map<String, Integer> followerData;

    public PacketContainer(Map<String, Integer> followerData) {
        this.followerData = followerData;
    }

    public static void encode(PacketContainer message, PacketBuffer buffer) {
        buffer.writeInt(message.followerData.size());
        for (Map.Entry<String, Integer> entry : message.followerData.entrySet()) {
            buffer.writeString(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
    }

    public static PacketContainer decode(PacketBuffer buffer) {
        Map<String, Integer> followerData = new HashMap<>();
        int n = buffer.readInt();
        for (int i = 0; i < n; i++) {
            String name = buffer.readString();
            int follower = buffer.readInt();
            followerData.put(name, follower);
        }
        return new PacketContainer(followerData);
    }

    public static boolean handle(PacketContainer message, Supplier<NetworkEvent.Context> supplier, ChatSizeManager manager) {
        supplier.get().enqueueWork(() -> manager.loadFollowerData(message.followerData));
        return true;
    }
}
