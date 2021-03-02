package net.kunmc.lab.chatsizechangemod;

import net.kunmc.lab.chatsizechangemod.config.ChatSizeChangeModConfig;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ChatSizeManager {
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
        return Math.random() + 0.5;
    }
}
