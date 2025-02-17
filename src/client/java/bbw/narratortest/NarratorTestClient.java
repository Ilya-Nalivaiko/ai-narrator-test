package bbw.narratortest;

import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class NarratorTestClient implements ClientModInitializer {
    private static ArrayList<String> armorState = new ArrayList<String>();

    private static long nextAllowedBlockUse = 0;
    private static long nextAllowedBlockPlace = 0;
    private static long nextAllowedTrade = 0;
    private static long nextAllowedBreed = 0;

    public static void clear(){
        armorState = new ArrayList<String>();
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(EventCalls::onClientTick);
    CustomSounds.initialize();

    // TTS audio stuff
    PayloadTypeRegistry.playS2C().register(TtsPayload.ID, TtsPayload.CODEC);
    ClientPlayNetworking.registerGlobalReceiver(
        TtsPayload.ID,
        (payload, context) -> {

          {
            // Read the remaining bytes (which contain your audio data)
            byte[] audioBytes = payload.audioData();
            context.client().execute(() -> {
              try {
                // Wrap the byte buffer in a stream and create an AudioInputStream
                ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                AudioInputStream ais = new AudioInputStream(bais, format, audioBytes.length);

                // Obtain a Clip, open it, and play
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();


              } catch (Exception e) {
                e.printStackTrace();
              }
            });
          }
        });







        for (int i=0; i<4; i++){
            armorState.add("");
        }

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
            if (world.isClient && player instanceof ClientPlayerEntity) {
                BlockPos interactedPos = hitResult.getBlockPos();
                BlockState interactedState = world.getBlockState(interactedPos);
                Block interactedBlock = interactedState.getBlock();

                // Check if the block is interactable (e.g., chest, furnace)
                if (isInteractableBlock(interactedBlock)) {
                    if(System.currentTimeMillis() > nextAllowedBlockUse){
                        // Log the block interaction
                        NarratorTest.sendLogSuccessMessage("You interacted with a block: " + Registries.BLOCK.getEntry(interactedBlock).getIdAsString() + " at " + interactedPos.toShortString(), player);
                        NarratorTest.addEvent(player, "Interact Block", Registries.BLOCK.getEntry(interactedBlock).getIdAsString(), System.currentTimeMillis());
                        nextAllowedBlockUse = System.currentTimeMillis() + 150;
                    } else NarratorTest.sendLogFailMessage("Remaining block interaction cooldown: " + Long.toString(nextAllowedBlockUse-System.currentTimeMillis()), player);
                    } else {
                    // Check if the player is placing a block
                    ItemStack stack = player.getStackInHand(hand);
                    if (stack.getItem() instanceof BlockItem) {
                        if(System.currentTimeMillis() > nextAllowedBlockPlace){
                            BlockPos placementPos = hitResult.getBlockPos().offset(hitResult.getSide());
                            NarratorTest.sendLogSuccessMessage("You placed a block: " + stack.getRegistryEntry().getIdAsString() + " at " + placementPos.toShortString(), player);
                            NarratorTest.addEvent(player, "Place Block", stack.getRegistryEntry().getIdAsString(), System.currentTimeMillis());
                            nextAllowedBlockPlace = System.currentTimeMillis() + 190;
                        } else NarratorTest.sendLogFailMessage("Remaining block place cooldown: " + Long.toString(nextAllowedBlockPlace-System.currentTimeMillis()), player);
                        }
                }
            }
            return ActionResult.PASS; // Allow the interaction to proceed
        });


        // Equip armor event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                int i = 0;
                for (ItemStack stack : player.getArmorItems()) {
                    if (!stack.isEmpty() && armorState.get(i).isEmpty()) {
                        armorState.set(i, stack.getRegistryEntry().getIdAsString());
                        NarratorTest.sendLogSuccessMessage("You equipped: " + stack.getRegistryEntry().getIdAsString(), player);
                        NarratorTest.addEvent(player, "Equip", stack.getRegistryEntry().getIdAsString(), System.currentTimeMillis());
                        
                    } else if (stack.isEmpty() && !armorState.get(i).isEmpty()){
                        NarratorTest.sendLogSuccessMessage("You unequipped: " + armorState.get(i), player);
                        NarratorTest.addEvent(player, "Unequip", armorState.get(i), System.currentTimeMillis());
                        armorState.set(i, "");
                    }
                    i++;
                }
            }
        });

        // Breeding and taming animals, and trading with villagers event
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof AnimalEntity) {
                AnimalEntity animal = (AnimalEntity) entity;
                ItemStack heldItem = player.getStackInHand(hand);

                if (animal.isBreedingItem(heldItem)) {
                    if(System.currentTimeMillis() > nextAllowedBreed){
                        NarratorTest.sendLogSuccessMessage("You bred: " + Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString() + " using " + heldItem.getName().getString(), player);
                        NarratorTest.addEvent(player, "Bred", Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString() + " using " + heldItem.getName().getString(), System.currentTimeMillis());
                        nextAllowedBreed = System.currentTimeMillis() + 300;
                    } else NarratorTest.sendLogFailMessage("Remaining breed cooldown: " + Long.toString(nextAllowedBreed-System.currentTimeMillis()), player);
                    
                } else {
                    NarratorTest.sendLogFailMessage("You tried to breed: " + Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString() + " with an invalid item", player);
                }
            }
            if (entity instanceof net.minecraft.entity.passive.VillagerEntity) {
                if(System.currentTimeMillis() > nextAllowedTrade){
                    NarratorTest.sendLogSuccessMessage("You traded with : " + Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString(), player);
                    NarratorTest.addEvent(player, "Traded with", Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString(), System.currentTimeMillis());
                    nextAllowedTrade = System.currentTimeMillis() + 500;
                } else NarratorTest.sendLogFailMessage("Remaining trade cooldown: " + Long.toString(nextAllowedTrade-System.currentTimeMillis()), player);
                
            }
            return ActionResult.PASS;
        });
    }

    private static boolean isInteractableBlock(Block block) {
    // List of interactable blocks
    return block instanceof ChestBlock || // Chests
           block instanceof FurnaceBlock || // Furnaces
           block instanceof CraftingTableBlock || // Crafting tables
           block instanceof AnvilBlock || // Anvils
           block instanceof EnchantingTableBlock || // Enchanting tables
           block instanceof EnderChestBlock || // Ender chests
           block instanceof ShulkerBoxBlock || // Shulker boxes
           block instanceof BarrelBlock || // Barrels
           block instanceof DispenserBlock || // Dispensers
           block instanceof DropperBlock || // Droppers
           block instanceof HopperBlock || // Hoppers
           block instanceof BrewingStandBlock || // Brewing stands
           block instanceof BeaconBlock || // Beacons
           block instanceof ComparatorBlock || // Redstone comparators
           block instanceof RepeaterBlock || // Redstone repeaters
           block instanceof LeverBlock || // Levers
           block instanceof ButtonBlock || // Buttons
           block instanceof DoorBlock || // Doors
           block instanceof TrapdoorBlock || // Trapdoors
           block instanceof FenceGateBlock || // Fence gates
           block instanceof NoteBlock || // Note blocks
           block instanceof JukeboxBlock || // Jukeboxes
           block instanceof LecternBlock || // Lecterns
           block instanceof LoomBlock || // Looms
           block instanceof StonecutterBlock || // Stonecutters
           block instanceof SmithingTableBlock || // Smithing tables
           block instanceof CartographyTableBlock || // Cartography tables
           block instanceof GrindstoneBlock || // Grindstones
           block instanceof BellBlock || // Bells
           block instanceof CampfireBlock || // Campfires
           block instanceof RespawnAnchorBlock || // Respawn anchors
           block instanceof CommandBlock || // Command blocks
           block instanceof StructureBlock || // Structure blocks
           block instanceof JigsawBlock || // Jigsaw blocks
           block instanceof AbstractSignBlock || // Signs
           block instanceof AbstractBannerBlock; // Banners
    }
}