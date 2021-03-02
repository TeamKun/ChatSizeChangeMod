package net.kunmc.lab.chatsizechangemod;

import net.minecraft.util.text.ITextComponent;

public class ChatLine {
    private final int updateCounterCreated;
    private final ITextComponent lineString;
    private final int chatLineID;
    private final double chatScale;

    public ChatLine(int updateCounterCreatedIn, ITextComponent lineStringIn, int chatLineIDIn, double chatScale) {
        this.lineString = lineStringIn;
        this.updateCounterCreated = updateCounterCreatedIn;
        this.chatLineID = chatLineIDIn;
        this.chatScale = chatScale;
    }

    public ITextComponent getChatComponent() {
        return this.lineString;
    }

    public int getUpdatedCounter() {
        return this.updateCounterCreated;
    }

    public int getChatLineID() {
        return this.chatLineID;
    }

    public double getChatScale() {
        return chatScale;
    }
}
