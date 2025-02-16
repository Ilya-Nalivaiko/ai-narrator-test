package bbw.narratortest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


public class EventCalls {
    private static boolean wasAlive = true; // Track the player's previous alive state
    private static int previousHurtTime = 0;

    private static ItemStack lastCraftedItem = ItemStack.EMPTY;
    private static boolean itemCraftedFlag = false;

    // Called on client tick
    public static void onClientTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (client.player == null) {
            return; // No player, do nothing
        }

        itemCrafted(player);

        // Check if the player was alive and is now dead
        if (wasAlive && player.isDead()) {
            onPlayerDeath(player); // Call the custom function
        }

        // Update the player's alive state
        wasAlive = !player.isDead();

        // Check if the player's hurt time has changed
        if (player.hurtTime > 0 && previousHurtTime == 0) {
            onPlayerDamaged(player, player.getRecentDamageSource());
        }
        previousHurtTime = player.hurtTime; // Update the previous hurt time
    }

    private static void onPlayerDamaged(PlayerEntity player, DamageSource source){
        player.sendMessage(Text.literal("[DEBUG] You took damage from " + source.getName()), false);
        NarratorTest.eventLogger.appendEvent("Took damage from", source.getName(), System.currentTimeMillis());
    }

    private static void onPlayerDeath(PlayerEntity player) {
        player.sendMessage(Text.literal("[DEBUG] You died"), false);
        NarratorTest.eventLogger.appendEvent("Took damage", "and died", System.currentTimeMillis());
    }

    private static void itemCrafted(ClientPlayerEntity player) {
        if (player.currentScreenHandler instanceof CraftingScreenHandler) {
            CraftingScreenHandler craftingScreenHandler = (CraftingScreenHandler) player.currentScreenHandler;
            ItemStack outputSlotStack = craftingScreenHandler.getSlot(0).getStack();

            if (!outputSlotStack.isEmpty() && !ItemStack.areEqual(outputSlotStack, lastCraftedItem)) {
                lastCraftedItem = outputSlotStack.copy();
                itemCraftedFlag = true;
            } else if (outputSlotStack.isEmpty() && itemCraftedFlag) {
                for (ItemStack stack : player.getInventory().main) {
                    if (ItemStack.areEqual(stack, lastCraftedItem)) {
                        if (!lastCraftedItem.isEmpty()) {
                            player.sendMessage(Text.literal("[DEBUG] You just crafted: " + lastCraftedItem.getName().getString()), false);
                            NarratorTest.eventLogger.appendEvent("Craft Item", lastCraftedItem.getName().getString(), System.currentTimeMillis());
                            break;
                        }

                    }
                }
                lastCraftedItem = ItemStack.EMPTY; // Reset the last crafted item
                itemCraftedFlag = false;
            }
        }
    }

    // Handles starting to use an item
    public static void onStartUsingItem(ItemStack stack, World world, PlayerEntity player) {
        // Only log non-block items to avoid duplication with block placement detection
        if (!(stack.getItem() instanceof BlockItem)) {
            player.sendMessage(Text.literal("[DEBUG] You are using: " + stack.getName().getString()), false);
            NarratorTest.eventLogger.appendEvent("Use Item", stack.getName().getString(), System.currentTimeMillis());
        }
    }

    // Handles attacking entities
    public static ActionResult onEntityDamage(PlayerEntity player, World world, Hand hand, Entity entity,
            @Nullable EntityHitResult hitResult) {
        if (entity instanceof LivingEntity) {
            player.sendMessage(Text.literal("[DEBUG] You just hit: " + entity.getName().getString()), false);
            NarratorTest.eventLogger.appendEvent("Hit Entity", entity.getName().getString(), System.currentTimeMillis());
        }
        return ActionResult.PASS;
    }
}