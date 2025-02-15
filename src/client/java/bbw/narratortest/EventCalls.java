package bbw.narratortest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;


public class EventCalls {
    private static ItemStack lastCraftedItem = ItemStack.EMPTY;
    private static boolean itemCraftedFlag = false;

    public static void onClientTick(MinecraftClient client) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.currentScreenHandler instanceof CraftingScreenHandler) {
            CraftingScreenHandler craftingScreenHandler = (CraftingScreenHandler) player.currentScreenHandler;
            ItemStack outputSlotStack = craftingScreenHandler.getSlot(0).getStack();

            if (!outputSlotStack.isEmpty() && !ItemStack.areEqual(outputSlotStack, lastCraftedItem) && !isAir(outputSlotStack)) {
                lastCraftedItem = outputSlotStack.copy();
                itemCraftedFlag = true;
            } else if (outputSlotStack.isEmpty() && itemCraftedFlag) {
                itemCrafted(player);
                lastCraftedItem = ItemStack.EMPTY; // Reset the last crafted item
                itemCraftedFlag = false;
            }
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
    }
    

    private static boolean isAir(ItemStack stack) {
        return stack.isEmpty();
    }
}
