package net.kunmc.lab.chatsizechangemod;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
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
    private final ChatSizeChangeMod modInstance;

    public NewChatGuiExt(Minecraft mcIn, ChatSizeChangeMod mod) {
        super(mcIn);
        this.mc = mcIn;
        this.modInstance = mod;
    }

    public void render(int updateCounter) {
        if (!isChatVisible()) {
            return;
        }
        int height = getChatHeight();
        if (drawnChatLines.isEmpty()) {
            return;
        }
        boolean isChatOpen = getChatOpen();
        double scale = getScale();
        int width = MathHelper.ceil((double)getChatWidth() / scale);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(2.0F, 8.0F, 0.0F);
        RenderSystem.scaled(scale, scale, 1.0D);
        double opacity = mc.gameSettings.chatOpacity * (double)0.9F + (double)0.1F;
        double backgroundOpacity = mc.gameSettings.accessibilityTextBackgroundOpacity;
        Matrix4f matrix4f = Matrix4f.makeTranslate(0.0F, 0.0F, -100.0F);
        double renderLength = 0;
        for (int i = 0; i + scrollPos < drawnChatLines.size(); i++) {
            ChatLine chatline = drawnChatLines.get(i + scrollPos);
            if (chatline != null) {
                double chatScale = chatline.getChatScale();
                int counter = updateCounter - chatline.getUpdatedCounter();
                double lineHeight = 1 + 8 * chatScale;
                if (counter < 200 || isChatOpen) {
                    double lineBrightness = isChatOpen ? 1.0D : getLineBrightness(counter);
                    int brightness = (int)(255.0D * lineBrightness * opacity);
                    int backgroundBrightness = (int)(255.0D * lineBrightness * backgroundOpacity);
                    if (renderLength + lineHeight > height) {
                        break;
                    }
                    if (brightness > 3) {
                        fill(matrix4f, -2, (int)-(renderLength + lineHeight), width + 4, (int)-renderLength, backgroundBrightness << 24);
                        String text = chatline.getChatComponent().getFormattedText();
                        RenderSystem.pushMatrix();
                        RenderSystem.enableBlend();
                        RenderSystem.translated(0, -(renderLength + lineHeight - 1), 0);
                        RenderSystem.scaled(chatScale, chatScale, 1.0D);
                        mc.fontRenderer.drawStringWithShadow(text, 0.0F, 0.0F, 16777215 + (brightness << 24));
                        RenderSystem.disableAlphaTest();
                        RenderSystem.disableBlend();
                        RenderSystem.popMatrix();
                    }
                    renderLength += lineHeight;
                }
            }
        }
        double scrolledLength = 0;
        double chatLength = 0;
        for (int i = 0; i < drawnChatLines.size(); i++) {
            ChatLine chatline = drawnChatLines.get(i);
            if (chatline != null) {
                double chatScale = chatline.getChatScale();
                double lineHeight = 1 + 8 * chatScale;
                chatLength += lineHeight;
                if (i < scrollPos) {
                    scrolledLength += lineHeight;
                }
            }
        }
        if (isChatOpen && chatLength != renderLength) {
            int pos = (int)(scrolledLength * renderLength / chatLength);
            int length = (int)(renderLength * renderLength / chatLength);
            int alpha = pos > 0 ? 170 : 96;
            int color = isScrolled ? 13382451 : 3355562;
            RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
            fill(0, -pos, 2, -length - pos, color + (alpha << 24));
            fill(1, -pos, 2, -length - pos, 13421772 + (alpha << 24));
        }
        RenderSystem.popMatrix();
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
        double chatScale = modInstance.getChatSizeManager().calcChatScale(chatComponent);
        setChatLine(chatComponent, chatLineId, updateCounter, displayOnly, chatScale);
    }

    private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, double chatScale) {
        if (chatLineId != 0) {
            deleteChatLine(chatLineId);
        }
        int width = MathHelper.floor((double)getChatWidth() / getScale());
        width = (int)(width / chatScale);
        List<ITextComponent> list = RenderComponentsUtil.splitText(chatComponent, width, mc.fontRenderer, false, false);
        boolean flag = getChatOpen();
        for (ITextComponent itextcomponent : list) {
            if (flag && scrollPos > 0) {
                isScrolled = true;
                addScrollPos(1.0D);
            }
            drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId, chatScale));
        }
        while (drawnChatLines.size() > 100) {
            drawnChatLines.remove(drawnChatLines.size() - 1);
        }
        if (!displayOnly) {
            chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId, chatScale));
            while (chatLines.size() > 100) {
                chatLines.remove(chatLines.size() - 1);
            }
        }
    }

    public void refreshChat() {
        drawnChatLines.clear();
        resetScroll();
        for (int i = chatLines.size() - 1; i >= 0; i--) {
            ChatLine chatline = chatLines.get(i);
            setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true, chatline.getChatScale());
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
        if (!getChatOpen() || mc.gameSettings.hideGUI || !isChatVisible()) {
            return null;
        }
        double scale = getScale();
        double cx = x - 2.0D;
        double cy = (double)mc.getMainWindow().getScaledHeight() - y - 40.0D;
        cx = MathHelper.floor(cx / scale);
        cy = MathHelper.floor(cy / scale);
        if (cx < 0.0D || cy < 0.0D) {
            return null;
        }
        double height = getChatHeight();
        double renderLength = 0;
        ChatLine line = null;
        for (int i = scrollPos; i < drawnChatLines.size(); i++) {
            ChatLine chatline = drawnChatLines.get(i);
            if (chatline != null) {
                double chatScale = chatline.getChatScale();
                double lineHeight = 1 + 8 * chatScale;
                renderLength += lineHeight;
                if (renderLength > cy) {
                    line = chatline;
                    break;
                }
                if (renderLength > height) {
                    return null;
                }
            }
        }
        if (line == null) {
            return null;
        }
        double chatScale = line.getChatScale();
        int width = 0;
        for (ITextComponent component : line.getChatComponent()) {
            if (component instanceof StringTextComponent) {
                width += mc.fontRenderer.getStringWidth(RenderComponentsUtil.removeTextColorsIfConfigured(((StringTextComponent)component).getText(), false));
                if (chatScale * width > cx) {
                    return component;
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
