package bbw.narratortest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.CraftingScreenHandler;
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

    public static void clear(){
        wasAlive = true; // Track the player's previous alive state
        previousHurtTime = 0;

        lastCraftedItem = ItemStack.EMPTY;
        itemCraftedFlag = false;
    }

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
        NarratorTest.sendLogSuccessMessage("You took damage from " + source.getTypeRegistryEntry().getIdAsString(), player);
        NarratorTest.addEvent(player, "Took damage from", source.getTypeRegistryEntry().getIdAsString(), System.currentTimeMillis());
    }

    private static void onPlayerDeath(PlayerEntity player) {
        NarratorTest.sendLogSuccessMessage("You died", player);
        NarratorTest.addEvent(player, "Died", "death", System.currentTimeMillis());
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
                            NarratorTest.sendLogSuccessMessage("You just crafted: " + lastCraftedItem.getRegistryEntry().getIdAsString(), player);
                            NarratorTest.addEvent(player, "Craft Item", lastCraftedItem.getRegistryEntry().getIdAsString(), System.currentTimeMillis());
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
            NarratorTest.sendLogSuccessMessage("You are using: " + stack.getRegistryEntry().getIdAsString(), player);
            NarratorTest.addEvent(player, "Use Item", stack.getRegistryEntry().getIdAsString(), System.currentTimeMillis());
        }
    }

    // Handles attacking entities
    public static ActionResult onEntityDamage(PlayerEntity player, World world, Hand hand, Entity entity,
            @Nullable EntityHitResult hitResult) {
        if (entity instanceof LivingEntity) {
            NarratorTest.sendLogSuccessMessage("You just hit: " + Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString() + " with " + player.getMainHandStack().getRegistryEntry().getIdAsString(), player);
            NarratorTest.addEvent(player, "Hit Entity", Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString() + " with " + player.getMainHandStack().getRegistryEntry().getIdAsString(), System.currentTimeMillis());
        }
        return ActionResult.PASS;
    }
}