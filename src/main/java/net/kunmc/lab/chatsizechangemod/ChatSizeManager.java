package net.kunmc.lab.chatsizechangemod;

import net.kunmc.lab.chatsizechangemod.config.ChatSizeChangeModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
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

    public double calcChatScale(ITextComponent chatComponent) {
        double defaultChatSize = ChatSizeChangeModConfig.DEFAULT_CHAT_SIZE.get();
        if (chatComponent instanceof TranslationTextComponent) {
            TranslationTextComponent translation = (TranslationTextComponent)chatComponent;
            String key = translation.getKey();
            if (key.equals("chat.type.text")) {
                List<ITextComponent> siblings = ((StringTextComponent)translation.getFormatArgs()[0]).getSiblings();
                String name = ((StringTextComponent)siblings.get(0)).getText();

                String message = ((StringTextComponent)translation.getFormatArgs()[1]).getUnformattedComponentText();
                if (message.matches("[0-9]+.*")) {
                    return Double.parseDouble(message.contains(" ") ? message.substring(0, message.indexOf(" ")) : message);
                } //デバッグ用

                return calcChatScale(name);
            }
        }
        return defaultChatSize;
    }

    public double calcChatScale(String playerName) {
        double defaultChatSize = ChatSizeChangeModConfig.DEFAULT_CHAT_SIZE.get();
        if (!isLoadedScoreboard || !scoreboardDataMap.containsKey(playerName)) {
            return defaultChatSize;
        }
        int followers = scoreboardDataMap.get(playerName);
        return Math.log10(followers + 1); //仮
    }

    public void loadScoreboard() {
        scoreboardDataMap = new HashMap<>();
        ClientWorld world = Objects.requireNonNull(Minecraft.getInstance().world);
        Scoreboard scoreboard = world.getScoreboard();
        ScoreObjective objective = scoreboard.getObjective("twitter");
        if (objective == null) {
            return;
        }
        for (Score score : scoreboard.getSortedScores(objective)) {
            scoreboardDataMap.put(score.getPlayerName(), score.getScorePoints());
        }
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
