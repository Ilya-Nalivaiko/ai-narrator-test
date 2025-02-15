package bbw.narratortest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;


public class EventCalls {
    private static ItemStack lastCraftedItem = ItemStack.EMPTY;
    private static boolean itemCraftedFlag = false;

    // Called on client tick
    public static void onClientTick(MinecraftClient client) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            itemCrafted(player);
        }
    }


    private static void itemCrafted(ClientPlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (ItemStack.areEqual(stack, lastCraftedItem)) {
                String craftedItemName = lastCraftedItem.getName().getString();
                player.sendMessage(Text.literal("You just crafted: " + craftedItemName), false);
    
                // Print to console for debugging
                System.out.println("[DEBUG] Sending request to ChatGPT: The player crafted: " + craftedItemName);
    
                // Run GPT request asynchronously
                CompletableFuture.supplyAsync(() -> {
                    String narration = ChatGPTTest.getNarration("The player crafted: " + craftedItemName);
                    
                    // Debugging: Print GPT response to console
                    System.out.println("[DEBUG] GPT Response: " + narration);
    
                    return narration;
                }).thenAccept(narration -> {
                    if (narration == null || narration.isEmpty()) {
                        System.out.println("[ERROR] GPT returned an empty response.");
                    } else {
                        // Send the GPT-generated narration to the player
                        player.sendMessage(Text.literal(narration), false);
                    }
                }).exceptionally(ex -> {
                    System.out.println("[ERROR] Error in GPT request: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    
                break;
            }
        }
    // Handles starting to use an item
    public static void onStartUsingItem(ItemStack stack, World world, PlayerEntity player) {
        player.sendMessage(Text.literal("You are using: " + stack.getName().getString()), false);
    }
    

    // Handles attacking entities
    public static ActionResult onEntityDamage(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (entity instanceof LivingEntity) {
            player.sendMessage(Text.literal("You just hit: " + entity.getName().getString()), false);
        }
        return ActionResult.PASS;
    }
}