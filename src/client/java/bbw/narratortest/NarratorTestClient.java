package bbw.narratortest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class NarratorTestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register client commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            DebugCommand.register(dispatcher);
        });

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(EventCalls::onClientTick);

        // Register attack entity event
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Only process the event if it's not canceled and the player is not spectating
            if (!world.isClient && player.isAttackable() && !player.isSpectator()) {
                ActionResult result = EventCalls.onEntityDamage(player, world, hand, entity, hitResult);
                return result;
            }
            return ActionResult.PASS;
        });

        // Register block interaction event (client-side)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient && player != null) {
                // Log the block interaction
                player.sendMessage(Text.literal("[DEBUG] You interacted with a block at " + hitResult.getBlockPos().toShortString()), false);
                NarratorTest.eventLogger.appendEvent("Interact Block", hitResult.getBlockPos().toShortString(), System.currentTimeMillis());

                // Check if the player is placing a block
                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem() instanceof BlockItem) {
                    BlockPos placementPos = hitResult.getBlockPos().offset(hitResult.getSide());
                    player.sendMessage(Text.literal("[DEBUG] You placed a block: " + stack.getName().getString() + " at " + placementPos.toShortString()), false);
                    NarratorTest.eventLogger.appendEvent("Place Block", stack.getName().getString(), System.currentTimeMillis());
                }
            }
            return ActionResult.PASS; // Allow the interaction to proceed
        });
    }
}