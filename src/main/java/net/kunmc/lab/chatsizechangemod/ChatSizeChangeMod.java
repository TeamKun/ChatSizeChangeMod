package net.kunmc.lab.chatsizechangemod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.kunmc.lab.chatsizechangemod.config.ChatSizeChangeModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mod("chatsizechangemod")
@OnlyIn(Dist.CLIENT)
public class ChatSizeChangeMod {
    private final ChatSizeManager chatSizeManager;

    public ChatSizeChangeMod() {
        this.chatSizeManager = new ChatSizeManager();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ChatSizeChangeModConfig.getConfig());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        dispatch("chatsizechange", "followers", "1");
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

    public void dispatch(String namespace, String path, String version) {
        ResourceLocation location = new ResourceLocation(namespace, path);
        SimpleChannel channel = NetworkRegistry.ChannelBuilder
                .named(location)
                .clientAcceptedVersions(NetworkRegistry.ACCEPTVANILLA::equals)
                .serverAcceptedVersions(NetworkRegistry.ACCEPTVANILLA::equals)
                .networkProtocolVersion(() -> version)
                .simpleChannel();
        channel.messageBuilder(PacketContainer.class, 0)
                .encoder(PacketContainer::encode)
                .decoder(PacketContainer::decode)
                .consumer((PacketContainer message, Supplier<NetworkEvent.Context> supplier) -> PacketContainer.handle(message, supplier, getChatSizeManager()))
                .add();
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

    @SubscribeEvent
    public void onLoggedIn(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        if (chatSizeManager.isLoadedFollowerData()) {
            chatSizeManager.unloadFollowerData();
        }
    }

    public ChatSizeManager getChatSizeManager() {
        return chatSizeManager;
    }
}
