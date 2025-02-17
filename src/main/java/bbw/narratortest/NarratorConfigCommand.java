package bbw.narratortest;

import bbw.narratortest.config.ModConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class NarratorConfigCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("narratorconfig")
            .then(CommandManager.literal("temperature")
                .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                    .executes(ctx -> {
                        float temp = FloatArgumentType.getFloat(ctx, "value");
                        ModConfig.getConfig().temperature = temp;
                        ModConfig.saveConfig(); // ✅ Save after change
                        ctx.getSource().sendFeedback(() -> Text.literal("Narrator temperature set to: " + temp), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(CommandManager.literal("cooldown")
                .then(CommandManager.argument("value", IntegerArgumentType.integer(1, 3600))
                    .executes(ctx -> {
                        int cooldown = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.getConfig().narratorCooldown = cooldown;
                        ModConfig.saveConfig(); // ✅ Save after change
                        ctx.getSource().sendFeedback(() -> Text.literal("Narrator cooldown set to: " + cooldown), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(CommandManager.literal("maxevents")
                .then(CommandManager.argument("value", IntegerArgumentType.integer(1, 3600))
                    .executes(ctx -> {
                        int maxevents = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.getConfig().maxEvents = maxevents;
                        ModConfig.saveConfig(); // ✅ Save after change
                        ctx.getSource().sendFeedback(() -> Text.literal("Narrator max events set to: " + maxevents), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(CommandManager.literal("debugLevel")
                .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 2))
                    .executes(ctx -> {
                        int level = IntegerArgumentType.getInteger(ctx, "value");
                        ModConfig.getConfig().debugLevel = level;
                        ModConfig.saveConfig(); // ✅ Save after change
                        ctx.getSource().sendFeedback(() -> Text.literal("Debug level set to: " + level), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(CommandManager.literal("prompt")
                .then(CommandManager.argument("value", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String prompt = StringArgumentType.getString(ctx, "value");
                        ModConfig.getConfig().systemPrompt = prompt;
                        ModConfig.saveConfig(); // ✅ Save after change
                        ctx.getSource().sendFeedback(() -> Text.literal("Narrator prompt updated!"), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
        );
    }
}
