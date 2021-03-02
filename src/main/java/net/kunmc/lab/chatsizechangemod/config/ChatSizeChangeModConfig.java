package net.kunmc.lab.chatsizechangemod.config;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.util.text.*;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChatSizeChangeModConfig {
    private static final Map<String, ForgeConfigSpec.ConfigValue<?>> configs = new HashMap<>();
    private static final Map<String, Class<?>> configTypes = new HashMap<>();
    private static final ForgeConfigSpec config;
    public static final ForgeConfigSpec.ConfigValue<Double> DEFAULT_CHAT_SIZE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        DEFAULT_CHAT_SIZE = builder.define("chatSizeChangeMod.defaultChatSize", 1.0);
        configs.put("defaultChatSize", DEFAULT_CHAT_SIZE);
        configTypes.put("defaultChatSize", Double.class);
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
