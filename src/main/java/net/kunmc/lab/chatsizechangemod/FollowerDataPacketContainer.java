package net.kunmc.lab.chatsizechangemod;

import net.minecraft.network.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

public class FollowerDataPacketContainer {
    private final Map<String, Integer> followerData;

    public FollowerDataPacketContainer(Map<String, Integer> followerData) {
        this.followerData = followerData;
    }

    public static void encode(FollowerDataPacketContainer message, PacketBuffer buffer) {
        buffer.writeInt(message.followerData.size());
        for (Map.Entry<String, Integer> entry : message.followerData.entrySet()) {
            buffer.writeString(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
    }

    public static FollowerDataPacketContainer decode(PacketBuffer buffer) {
        Map<String, Integer> followerData = new HashMap<>();
        int n = buffer.readInt();
        for (int i = 0; i < n; i++) {
            String name = buffer.readString();
            int follower = buffer.readInt();
            followerData.put(name, follower);
        }
        return new FollowerDataPacketContainer(followerData);
    }

    public static boolean handle(ChatSizeManager manager, FollowerDataPacketContainer message) {
        manager.loadFollowerData(message.followerData);
        return true;
    }
}
