package net.kunmc.lab.chatsizechangemod;

import net.kunmc.lab.chatsizechangemod.config.ConfigChangePacketContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

@Mod("chatsizechangemod")
@OnlyIn(Dist.CLIENT)
public class ChatSizeChangeMod {
    private final ChatSizeManager chatSizeManager;

    public ChatSizeChangeMod() {
        this.chatSizeManager = new ChatSizeManager();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        dispatch("chatsizechange", "follower", "1")
                .messageBuilder(FollowerDataPacketContainer.class, 0)
                .encoder(FollowerDataPacketContainer::encode)
                .decoder(FollowerDataPacketContainer::decode)
                .consumer((FollowerDataPacketContainer message, Supplier<NetworkEvent.Context> supplier) -> FollowerDataPacketContainer.handle(getChatSizeManager(), message))
                .add();
        dispatch("chatsizechange", "config", "1")
                .messageBuilder(ConfigChangePacketContainer.class, 1)
                .encoder(ConfigChangePacketContainer::encode)
                .decoder(ConfigChangePacketContainer::decode)
                .consumer(ConfigChangePacketContainer::handle)
                .add();
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

    public SimpleChannel dispatch(String namespace, String path, String version) {
        return NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(namespace, path))
                .clientAcceptedVersions(NetworkRegistry.ACCEPTVANILLA::equals)
                .serverAcceptedVersions(NetworkRegistry.ACCEPTVANILLA::equals)
                .networkProtocolVersion(() -> version)
                .simpleChannel();
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!(Minecraft.getInstance().ingameGUI.getChatGUI() instanceof NewChatGuiExt)) {
            changeNewChatGui();
        }
    }

    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        if (chatSizeManager.isLoadedFollowerData()) {
            chatSizeManager.unloadFollowerData();
        }
    }

    public ChatSizeManager getChatSizeManager() {
        return chatSizeManager;
    }
}
