package net.kunmc.lab.chatsizechangemod;

import net.kunmc.lab.chatsizechangemod.config.ModConfig;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ChatSizeManager {
    public double calcChatScale(ITextComponent chatComponent) {
        Double defaultChatSize = ModConfig.defaultChatSize.get();
        if (chatComponent instanceof TranslationTextComponent) {
            TranslationTextComponent translation = (TranslationTextComponent)chatComponent;
            String key = translation.getKey();
            if (key.equals("chat.type.text")) {
                List<ITextComponent> siblings = ((StringTextComponent)translation.getFormatArgs()[0]).getSiblings();
                String name = ((StringTextComponent)siblings.get(0)).getText();
                String message = ((StringTextComponent)translation.getFormatArgs()[1]).getText();
                System.out.println(name + ", " + message);
            }
        }
        return Math.random() + 0.5; // デバッグ用
    }
}
