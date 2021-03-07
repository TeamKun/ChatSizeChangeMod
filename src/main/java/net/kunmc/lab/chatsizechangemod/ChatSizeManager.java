package net.kunmc.lab.chatsizechangemod;

import net.kunmc.lab.chatsizechangemod.config.ChatSizeChangeModConfig;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Map;

public class ChatSizeManager {
    private Map<String, Integer> followerData = null;
    private boolean isLoadedFollowerData;
    private double average;
    private double std;

    public double calcChatScale(ITextComponent chatComponent) {
        double defaultChatSize = ChatSizeChangeModConfig.DEFAULT_CHAT_SIZE.get();
        if (chatComponent instanceof TranslationTextComponent) {
            TranslationTextComponent translation = (TranslationTextComponent)chatComponent;
            String key = translation.getKey();
            if (key.equals("chat.type.text")) {
                List<ITextComponent> siblings = ((StringTextComponent)translation.getFormatArgs()[0]).getSiblings();
                String name = ((StringTextComponent)siblings.get(0)).getText();
                name = ((StringTextComponent)translation.getFormatArgs()[1]).getUnformattedComponentText();
                return calcChatScale(name);
            }
        }
        return defaultChatSize;
    }

    public double calcChatScale(String playerName) {
        double defaultChatSize = ChatSizeChangeModConfig.DEFAULT_CHAT_SIZE.get();
        double minChatSize = ChatSizeChangeModConfig.MIN_CHAT_SIZE.get();
        double maxChatSize = ChatSizeChangeModConfig.MAX_CHAT_SIZE.get();
        double chatSizeMultiply = ChatSizeChangeModConfig.CHAT_SIZE_MULTIPLY.get();
        double chatBaseSize = ChatSizeChangeModConfig.CHAT_BASE_SIZE.get();
        if (!isLoadedFollowerData || !followerData.containsKey(playerName)) {
            return defaultChatSize;
        }
        int followers = followerData.get(playerName);
        double chatScale = chatBaseSize + chatSizeMultiply * (followers - average) / std;
        return Math.min(Math.max(chatScale, minChatSize), maxChatSize);
    }

    public void loadFollowerData(Map<String, Integer> followerData) {
        int n = followerData.size();
        this.followerData = followerData;
        average = 0;
        std = 0;
        for (int follower : followerData.values()) {
            average += follower;
        }
        average /= n;
        for (int follower : followerData.values()) {
            std += (average - follower) * (average - follower);
        }
        std = Math.sqrt(std / n);
        isLoadedFollowerData = true;
    }

    public void unloadFollowerData() {
        followerData = null;
        isLoadedFollowerData = false;
    }

    public boolean isLoadedFollowerData() {
        return isLoadedFollowerData;
    }
}
