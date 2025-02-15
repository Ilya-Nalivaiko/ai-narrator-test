package bbw.narratortest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class NarratorTestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(EventCalls::onClientTick);

        // Register attack entity event
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ActionResult result = EventCalls.onEntityDamage(player, world, hand, entity, hitResult);
            return result;
        });

        // Register finish using item event
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient) { //TODO:Check if the item is food
                EventCalls.onFinishUsingItem(stack, world, player);
            }
            return ActionResult.PASS; // Return PASS to allow the item usage to proceed
        });
    }
}