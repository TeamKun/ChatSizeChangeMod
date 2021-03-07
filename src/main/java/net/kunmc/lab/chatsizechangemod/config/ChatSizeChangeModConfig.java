package net.kunmc.lab.chatsizechangemod.config;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChatSizeChangeModConfig {
    private static final Map<String, ForgeConfigSpec.ConfigValue<?>> configs = new HashMap<>();
    private static final Map<String, Class<?>> configTypes = new HashMap<>();
    private static final ForgeConfigSpec config;
    public static final ForgeConfigSpec.ConfigValue<Double> DEFAULT_CHAT_SIZE;
    public static final ForgeConfigSpec.ConfigValue<Double> MIN_CHAT_SIZE;
    public static final ForgeConfigSpec.ConfigValue<Double> MAX_CHAT_SIZE;
    public static final ForgeConfigSpec.ConfigValue<Double> CHAT_SIZE_MULTIPLY;
    public static final ForgeConfigSpec.ConfigValue<Double> CHAT_BASE_SIZE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        DEFAULT_CHAT_SIZE = builder.define("chatSizeChangeMod.defaultChatSize", 1.0);
        MIN_CHAT_SIZE = builder.define("chatSizeChangeMod.minChatSize", 0.0);
        MAX_CHAT_SIZE = builder.define("chatSizeChangeMod.maxChatSize", 3.0);
        CHAT_SIZE_MULTIPLY = builder.define("chatSizeChangeMod.chatSizeMultiply", 1.0);
        CHAT_BASE_SIZE = builder.define("chatSizeChangeMod.chatBaseSize", 1.0);
        configs.put("defaultChatSize", DEFAULT_CHAT_SIZE);
        configs.put("minChatSize", MIN_CHAT_SIZE);
        configs.put("maxChatSize", MAX_CHAT_SIZE);
        configs.put("chatSizeMultiply", CHAT_SIZE_MULTIPLY);
        configs.put("chatBaseSize", CHAT_BASE_SIZE);
        configTypes.put("defaultChatSize", Double.class);
        configTypes.put("minChatSize", Double.class);
        configTypes.put("maxChatSize", Double.class);
        configTypes.put("chatSizeMultiply", Double.class);
        configTypes.put("chatBaseSize", Double.class);
        config = builder.build();
    }

    @SuppressWarnings("unchecked")
    public static int setConfig(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "key");
        String value = StringArgumentType.getString(context, "value");
        if (!configs.containsKey(key)) {
            ITextComponent component = new StringTextComponent("[ChatSizeChangeMod] ")
                    .setStyle(new Style().setColor(TextFormatting.GREEN))
                    .appendSibling(new StringTextComponent("コンフィグの設定に失敗しました")
                            .setStyle(new Style().setColor(TextFormatting.RED)));
            throw new SimpleCommandExceptionType(component).create();
        }
        ForgeConfigSpec.ConfigValue<?> configValue = configs.get(key);
        try {
            if (configTypes.get(key) == Integer.class) {
                ((ForgeConfigSpec.ConfigValue<Integer>)configValue).set(Integer.valueOf(value));
            } else if (configTypes.get(key) == Double.class) {
                ((ForgeConfigSpec.ConfigValue<Double>)configValue).set(Double.valueOf(value));
            } else if (configTypes.get(key) == String.class) {
                ((ForgeConfigSpec.ConfigValue<String>)configValue).set(value);
            }
            ITextComponent component = new StringTextComponent("[ChatSizeChangeMod] ")
                    .setStyle(new Style().setColor(TextFormatting.GREEN))
                    .appendSibling(new StringTextComponent(key + "を" + value + "にセットしました")
                            .setStyle(new Style().setColor(TextFormatting.RESET)));
            context.getSource().sendFeedback(component, true);
            return 1;
        } catch (Exception e) {
            ITextComponent component = new StringTextComponent("[ChatSizeChangeMod] ")
                    .setStyle(new Style().setColor(TextFormatting.GREEN))
                    .appendSibling(new StringTextComponent("コンフィグの設定に失敗しました")
                            .setStyle(new Style().setColor(TextFormatting.RED)));
            throw new SimpleCommandExceptionType(component).create();
        }
    }

    public static CompletableFuture<Suggestions> suggestConfigKeys(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(configs.keySet(), builder);
    }

    public static ForgeConfigSpec getConfig() {
        return config;
    }

}
