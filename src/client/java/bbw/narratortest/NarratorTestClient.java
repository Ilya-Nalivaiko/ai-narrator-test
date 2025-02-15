package bbw.narratortest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;

public class NarratorTestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
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

        // Register item usage event (triggers at the start of item usage)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            EventCalls.onStartUsingItem(stack, world, player);
            return ActionResult.PASS; // Allow the item usage to proceed
        });
    }
}