package net.kunmc.lab.chatsizechangemod;

import net.kunmc.lab.chatsizechangemod.config.ChatSizeChangeModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatSizeManager {
    private Map<String, Integer> scoreboardDataMap = null;
    private boolean isLoadedScoreboard;
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
        if (!isLoadedScoreboard || !scoreboardDataMap.containsKey(playerName)) {
            return defaultChatSize;
        }
        int followers = scoreboardDataMap.get(playerName);
        double chatScale = chatBaseSize + chatSizeMultiply * (followers - average) / std;
        return Math.min(Math.max(chatScale, minChatSize), maxChatSize);
    }

    public void loadScoreboard() {
        scoreboardDataMap = new HashMap<>();
        IntegratedServer server = Objects.requireNonNull(Minecraft.getInstance().getIntegratedServer());
        Scoreboard scoreboard = server.getScoreboard();
        ScoreObjective objective = scoreboard.getObjective("twitter");
        if (objective == null) {
            return;
        }
        for (Score score : scoreboard.getSortedScores(objective)) {
            int follower = score.getScorePoints();
            average += follower;
            scoreboardDataMap.put(score.getPlayerName(), follower);
        }
        int n = scoreboardDataMap.size();
        average /= n;
        for (int follower : scoreboardDataMap.values()) {
            std += (average - follower) * (average - follower);
        }
        std = Math.sqrt(std / n);
        isLoadedScoreboard = true;
    }

    public void unloadScoreboard() {
        scoreboardDataMap = null;
        isLoadedScoreboard = false;
    }

    public boolean isLoadedScoreboard() {
        return isLoadedScoreboard;
    }
}
