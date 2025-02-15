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
            ClientCommandManager.literal("feedback")
                .executes(DebugCommand::executeFeedback)
        );
    }

    private static int executeFeedback(CommandContext<FabricClientCommandSource> context) {
        ClientPlayerEntity player = context.getSource().getPlayer();

        if (player != null) {
            // Do something with the player
            GTPInterface.getGPTFeedback(NarratorTest.eventLogger.collapseEvents(), player);
        } else {
            // Handle the case where the command was not executed by a player (e.g., from console)
            context.getSource().sendError(Text.of("This command can only be executed by a player."));
        }
        
        // Send feedback to the player
        context.getSource().sendFeedback(Text.of("Debug command executed!"));
        return 1;
    }
}