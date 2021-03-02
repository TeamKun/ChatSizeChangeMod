package net.kunmc.lab.chatsizechangemod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.kunmc.lab.chatsizechangemod.config.ChatSizeChangeModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.command.Commands;
import net.minecraft.command.impl.FillCommand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod("chatsizechangemod")
@OnlyIn(Dist.CLIENT)
public class ChatSizeChangeMod {
    private final ChatSizeManager chatSizeManager;

    public ChatSizeChangeMod() {
        this.chatSizeManager = new ChatSizeManager();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ChatSizeChangeModConfig.getConfig());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void changeNewChatGui() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            IngameGui ingameGUI = minecraft.ingameGUI;
            Field field = ObfuscationReflectionHelper.findField(IngameGui.class, "field_73840_e");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(ingameGUI, new NewChatGuiExt(minecraft, this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!(Minecraft.getInstance().ingameGUI.getChatGUI() instanceof NewChatGuiExt)) {
            changeNewChatGui();
        }
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        event.getCommandDispatcher().register(Commands.literal("cscconfig")
                .requires(source -> source.hasPermissionLevel(2))
                .then(Commands.argument("key", StringArgumentType.string())
                        .suggests(ChatSizeChangeModConfig::suggestConfigKeys)
                        .then(Commands.argument("value", StringArgumentType.string())
                                .executes(ChatSizeChangeModConfig::setConfig))));
    }

    public ChatSizeManager getChatSizeManager() {
        return chatSizeManager;
    }
}
