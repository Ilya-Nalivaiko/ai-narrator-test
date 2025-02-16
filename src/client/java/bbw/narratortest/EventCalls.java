package bbw.narratortest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
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
    private static ItemStack lastCraftedItem = ItemStack.EMPTY;
    private static boolean itemCraftedFlag = false;

    // Called on client tick
    public static void onClientTick(MinecraftClient client) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            itemCrafted(player);
        }
    }

    // Handles crafting events
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
                        player.sendMessage(Text.literal("You just crafted: " + lastCraftedItem.getName().getString()),
                                false);
                        break;
                    }
                }
                lastCraftedItem = ItemStack.EMPTY; // Reset the last crafted item
                itemCraftedFlag = false;
            }
        }
    }

    // Handles finishing using an item (e.g., eating food)
    public static void onFinishUsingItem(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;
            player.sendMessage(Text.literal("You just ate: " + stack.getName().getString()), false);
        }
    }

    // Handles attacking entities
    public static ActionResult onEntityDamage(PlayerEntity player, World world, Hand hand, Entity entity,
            @Nullable EntityHitResult hitResult) {
        if (entity instanceof LivingEntity) {
            player.sendMessage(Text.literal("You just hit: " + entity.getName().getString()), false);
        }
        return ActionResult.PASS;
    }

    // Handles killing entities
    public static void onEntityKill(PlayerEntity player, World world, LivingEntity killedEntity,
            @Nullable DamageSource source) {
        player.sendMessage(Text.literal("You just killed: " + killedEntity.getName().getString()), false);
    }
}