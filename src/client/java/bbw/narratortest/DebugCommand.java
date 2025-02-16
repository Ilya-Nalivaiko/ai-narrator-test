package bbw.narratortest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;


public class DebugCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("feedback_collapsed")
                .executes(DebugCommand::executeFeedbackCollapsed)
        );
        dispatcher.register(
            ClientCommandManager.literal("feedback_full")
                .executes(DebugCommand::executeFeedbackFull)
        );
        dispatcher.register(
            ClientCommandManager.literal("clearlog")
                .executes(DebugCommand::executeClear)
        );
    }

    private static int executeFeedbackCollapsed(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            GTPInterface.getGPTFeedback(NarratorTest.eventLogger.collapseEvents(), player);
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }
        
        // Send feedback to the player
        context.getSource().sendFeedback(Text.of("Feedback command executed!"));
        return 1;
    }

    private static int executeFeedbackFull(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            GTPInterface.getGPTFeedback(NarratorTest.eventLogger.dontCollapseEvents(), player);
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }
        
        // Send feedback to the player
        context.getSource().sendFeedback(Text.of("Feedback command executed!"));
        return 1;
    }

    private static int executeClear(CommandContext<FabricClientCommandSource> context){
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            NarratorTest.eventLogger.clear();
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }
        
        // Send feedback to the player
        context.getSource().sendFeedback(Text.of("Clear command executed!"));
        return 1;
    }
}