package bbw.narratortest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

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
                player.sendMessage(Text.literal("You just crafted: " + lastCraftedItem.getName().getString()), false);
                break;
            }
        }
    }

    private static boolean isAir(ItemStack stack) {
        return stack.isEmpty();
    }
}
