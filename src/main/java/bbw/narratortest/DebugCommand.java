package bbw.narratortest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DebugCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("feedback_collapsed")
                .executes(DebugCommand::executeFeedbackCollapsed)
        );
        dispatcher.register(
            CommandManager.literal("feedback_full")
                .executes(DebugCommand::executeFeedbackFull)
        );
        dispatcher.register(
            CommandManager.literal("clearlog")
                .executes(DebugCommand::executeClear)
        );
        dispatcher.register(
            CommandManager.literal("log_collapsed")
                .executes(DebugCommand::executeLogCollapsed)
        );
        dispatcher.register(
            CommandManager.literal("log_full")
                .executes(DebugCommand::executeLogFull)
        );
    }

    private static int executeFeedbackCollapsed(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            GTPInterface.getGPTFeedback(NarratorTest.eventLogger.collapseEvents(), player);
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }

        // Send feedback to the player
        context.getSource().sendFeedback(() -> Text.of("Feedback command executed!"), false);
        return 1;
    }

    private static int executeFeedbackFull(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            GTPInterface.getGPTFeedback(NarratorTest.eventLogger.dontCollapseEvents(), player);
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }

        // Send feedback to the player
        context.getSource().sendFeedback(() -> Text.of("Feedback command executed!"), false);
        return 1;
    }

    private static int executeClear(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            NarratorTest.eventLogger.clear();
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }

        // Send feedback to the player
        context.getSource().sendFeedback(() -> Text.of("Clear command executed!"), false);
        return 1;
    }

    private static int executeLogFull(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            context.getSource().sendFeedback(() -> Text.of(NarratorTest.eventLogger.dontCollapseEvents()), false);
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }

        // Send feedback to the player
        context.getSource().sendFeedback(() -> Text.of("Log command executed!"), false);
        return 1;
    }

    private static int executeLogCollapsed(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            context.getSource().sendFeedback(() -> Text.of(NarratorTest.eventLogger.collapseEvents()), false);
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }

        // Send feedback to the player
        context.getSource().sendFeedback(() -> Text.of("Log command executed!"), false);
        return 1;
    }
}