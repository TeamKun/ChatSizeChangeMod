package net.kunmc.lab.chatsizechangemod;

import net.kunmc.lab.chatsizechangemod.config.ChatSizeChangeModConfig;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatSizeManager {
    private Map<String, Integer> followerData = null;
    private boolean isLoadedFollowerData;
    private double average;
    private double std;
    private final Pattern chatPattern = Pattern.compile("<(\\w{3,16}+).*?> (.+)");

    public double calcChatScale(ITextComponent chatComponent) {
        double defaultChatSize = ChatSizeChangeModConfig.getDefaultChatSize();
        String text = chatComponent.stream()
                .map(ITextComponent::getUnformattedComponentText)
                .collect(Collectors.joining());
        Matcher matcher = chatPattern.matcher(text);
        if (matcher.matches()) {
            String name = matcher.group(1);
            return calcChatScale(name);
        }
        return defaultChatSize;
    }

    public double calcChatScale(String playerName) {
        double minChatSize = ChatSizeChangeModConfig.getMinChatSize();
        double maxChatSize = ChatSizeChangeModConfig.getMaxChatSize();
        double chatSizeMultiply = ChatSizeChangeModConfig.getChatSizeMultiply();
        double chatBaseSize = ChatSizeChangeModConfig.getChatBaseSize();
        if (!isLoadedFollowerData || !followerData.containsKey(playerName)) {
            return minChatSize;
        }
        int followers = followerData.get(playerName);
        double chatScale = chatBaseSize + chatSizeMultiply * (followers - average) / std;
        return Math.min(Math.max(chatScale, minChatSize), maxChatSize);
    }

    public void loadFollowerData(Map<String, Integer> data) {
        int n = data.size();
        followerData = data;
        average = 0;
        std = 0;
        for (int follower : data.values()) {
            average += follower;
        }
        average /= n;
        for (int follower : data.values()) {
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
