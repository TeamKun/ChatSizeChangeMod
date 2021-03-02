package net.kunmc.lab.chatsizechangemod;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class NewChatGuiExt extends NewChatGui {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.newArrayList();
    private final List<ChatLine> chatLines = Lists.newArrayList();
    private final List<ChatLine> drawnChatLines = Lists.newArrayList();
    private int scrollPos;
    private boolean isScrolled;

    public NewChatGuiExt(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
    }

    public void render(int updateCounter) {
        if (isChatVisible()) {
            int lineCount = getLineCount();
            int j = drawnChatLines.size();
            if (j > 0) {
                boolean flag = false;
                if (getChatOpen()) {
                    flag = true;
                }
                double scale = getScale();
                int k = MathHelper.ceil((double)getChatWidth() / scale);
                RenderSystem.pushMatrix();
                RenderSystem.translatef(2.0F, 8.0F, 0.0F);
                RenderSystem.scaled(scale, scale, 1.0D);
                double d1 = mc.gameSettings.chatOpacity * (double)0.9F + (double)0.1F;
                double d2 = mc.gameSettings.accessibilityTextBackgroundOpacity;
                int l = 0;
                Matrix4f matrix4f = Matrix4f.makeTranslate(0.0F, 0.0F, -100.0F);
                for (int i1 = 0; i1 + scrollPos < drawnChatLines.size() && i1 < lineCount; ++i1) {
                    ChatLine chatline = drawnChatLines.get(i1 + scrollPos);
                    if (chatline != null) {
                        int j1 = updateCounter - chatline.getUpdatedCounter();
                        if (j1 < 200 || flag) {
                            double d3 = flag ? 1.0D : getLineBrightness(j1);
                            int l1 = (int)(255.0D * d3 * d1);
                            int i2 = (int)(255.0D * d3 * d2);
                            ++l;
                            if (l1 > 3) {
                                int k2 = -i1 * 9;
                                fill(matrix4f, -2, k2 - 9, k + 4, k2, i2 << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                RenderSystem.enableBlend();
                                mc.fontRenderer.drawStringWithShadow(s, 0.0F, (float)(k2 - 8), 16777215 + (l1 << 24));
                                RenderSystem.disableAlphaTest();
                                RenderSystem.disableBlend();
                            }
                        }
                    }
                }
                if (flag) {
                    int l2 = 9;
                    RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
                    int i3 = j * l2 + j;
                    int j3 = l * l2 + l;
                    int k3 = scrollPos * j3 / j;
                    int k1 = j3 * j3 / i3;
                    if (i3 != j3) {
                        int l3 = k3 > 0 ? 170 : 96;
                        int i4 = isScrolled ? 13382451 : 3355562;
                        fill(0, -k3, 2, -k3 - k1, i4 + (l3 << 24));
                        fill(2, -k3, 1, -k3 - k1, 13421772 + (l3 << 24));
                    }
                }
                RenderSystem.popMatrix();
            }
        }
    }

    private boolean isChatVisible() {
        return mc.gameSettings.chatVisibility != ChatVisibility.HIDDEN;
    }

    private static double getLineBrightness(int counterIn) {
        double d0 = MathHelper.clamp((1.0D - (double)counterIn / 200.0D) * 10.0D, 0.0D, 1.0D);
        return d0 * d0;
    }

    public void clearChatMessages(boolean clearSentMsgHistory) {
        drawnChatLines.clear();
        chatLines.clear();
        if (clearSentMsgHistory) {
            sentMessages.clear();
        }
    }

    public void printChatMessage(ITextComponent chatComponent) {
        printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
        setChatLine(chatComponent, chatLineId, mc.ingameGUI.getTicks(), false);
        LOGGER.info("[CHAT] {}", chatComponent.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            deleteChatLine(chatLineId);
        }
        int i = MathHelper.floor((double)getChatWidth() / getScale());
        List<ITextComponent> list = RenderComponentsUtil.splitText(chatComponent, i, mc.fontRenderer, false, false);
        boolean flag = getChatOpen();
        for (ITextComponent itextcomponent : list) {
            if (flag && scrollPos > 0) {
                isScrolled = true;
                addScrollPos(1.0D);
            }
            drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId));
        }
        while (drawnChatLines.size() > 100) {
            drawnChatLines.remove(drawnChatLines.size() - 1);
        }
        if (!displayOnly) {
            chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));
            while (chatLines.size() > 100) {
                chatLines.remove(chatLines.size() - 1);
            }
        }
    }

    public void refreshChat() {
        drawnChatLines.clear();
        resetScroll();
        for (int i = chatLines.size() - 1; i >= 0; --i) {
            ChatLine chatline = chatLines.get(i);
            setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    public List<String> getSentMessages() {
        return sentMessages;
    }

    public void addToSentMessages(String message) {
        if (sentMessages.isEmpty() || !sentMessages.get(sentMessages.size() - 1).equals(message)) {
            sentMessages.add(message);
        }
    }

    public void resetScroll() {
        scrollPos = 0;
        isScrolled = false;
    }

    public void addScrollPos(double posInc) {
        scrollPos = (int)((double)scrollPos + posInc);
        int i = drawnChatLines.size();
        if (scrollPos > i - getLineCount()) {
            scrollPos = i - getLineCount();
        }
        if (scrollPos <= 0) {
            scrollPos = 0;
            isScrolled = false;
        }
    }

    @Nullable
    public ITextComponent getTextComponent(double x, double y) {
        if (getChatOpen() && !mc.gameSettings.hideGUI && isChatVisible()) {
            double scale = getScale();
            double d1 = x - 2.0D;
            double d2 = (double)mc.getMainWindow().getScaledHeight() - y - 40.0D;
            d1 = MathHelper.floor(d1 / scale);
            d2 = MathHelper.floor(d2 / scale);
            if (!(d1 < 0.0D) && !(d2 < 0.0D)) {
                int i = Math.min(getLineCount(), drawnChatLines.size());
                if (d1 <= (double)MathHelper.floor((double)getChatWidth() / getScale()) && d2 < (double)(9 * i + i)) {
                    int j = (int)(d2 / 9.0D + (double)scrollPos);
                    if (j >= 0 && j < drawnChatLines.size()) {
                        ChatLine chatline = drawnChatLines.get(j);
                        int k = 0;
                        for (ITextComponent itextcomponent : chatline.getChatComponent()) {
                            if (itextcomponent instanceof StringTextComponent) {
                                k += mc.fontRenderer.getStringWidth(RenderComponentsUtil.removeTextColorsIfConfigured(((StringTextComponent)itextcomponent).getText(), false));
                                if ((double)k > d1) {
                                    return itextcomponent;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean getChatOpen() {
        return mc.currentScreen instanceof ChatScreen;
    }

    public void deleteChatLine(int id) {
        Iterator<ChatLine> iterator = drawnChatLines.iterator();
        while (iterator.hasNext()) {
            ChatLine chatline = iterator.next();
            if (chatline.getChatLineID() == id) {
                iterator.remove();
            }
        }
        iterator = chatLines.iterator();
        while (iterator.hasNext()) {
            ChatLine chatline1 = iterator.next();
            if (chatline1.getChatLineID() == id) {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth() {
        return calculateChatboxWidth(mc.gameSettings.chatWidth);
    }

    public int getChatHeight() {
        return calculateChatboxHeight(getChatOpen() ? mc.gameSettings.chatHeightFocused : mc.gameSettings.chatHeightUnfocused);
    }

    public double getScale() {
        return mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(double chatWidth) {
        return MathHelper.floor(chatWidth * 280.0D + 40.0D);
    }

    public static int calculateChatboxHeight(double chatHeight) {
        return MathHelper.floor(chatHeight * 160.0D + 20.0D);
    }

    public int getLineCount() {
        return getChatHeight() / 9;
    }
}
