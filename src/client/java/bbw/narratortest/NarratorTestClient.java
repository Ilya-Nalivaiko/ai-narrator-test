package bbw.narratortest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import net.minecraft.util.math.random.Random;

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
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bbw.narratortest.config.ModConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class NarratorTestClient implements ClientModInitializer {
  private int nextChannel = 0;

  private static ArrayList<String> armorState = new ArrayList<String>();

  private static long nextAllowedBlockUse = 0;
  private static long nextAllowedBlockPlace = 0;
  private static long nextAllowedTrade = 0;
  private static long nextAllowedBreed = 0;

  public static void clear() {
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
          Entity narratorEntityRaw = context.client().world.getEntityById(payload.entityId());
          if (narratorEntityRaw == null || narratorEntityRaw.getClass() != ArmorStandEntity.class) {
            System.err.println("Narrator entity not found");
            return;
          }
          ArmorStandEntity narratorEntity = (ArmorStandEntity) narratorEntityRaw;
            
            byte[] audioBytes = payload.audioData();
            int channelIndex = nextChannel;
            nextChannel = (nextChannel + 1);
            if (nextChannel > 6) {
              nextChannel = 0;
              context.client().getSoundManager().reloadSounds();
            }

            SoundEvent soundEvent = CustomSounds.getChannel(channelIndex);
            String channelName = "narration_channel" + channelIndex;
            String wavName = channelName + ".wav";
            try (FileOutputStream fos = new FileOutputStream(wavName)) {
              fos.write(audioBytes);
              String oggName = channelName + ".ogg";
              // delete previous item occupying channel if it exists
              File oldChannelIntermediateContent = new File(oggName);
              if (oldChannelIntermediateContent.exists()) {
                oldChannelIntermediateContent.delete();
              }

              ProcessBuilder pb = new ProcessBuilder(
                  "ffmpeg", "-i", wavName, "-c:a", "libvorbis", oggName);
              Process process = pb.start();
              int exitCode = process.waitFor();
              if (exitCode == 0) {
                System.out.println("Conversion successful!");
              } else {
                System.err.println("Conversion failed with exit code: " + exitCode);
              }

              // remove old content and move new content to
              // resources/assets/narrator-test/sounds/
              final String assetLocation = "../build/resources/main/assets/narrator-test/sounds/";
              File newChannelContent = new File(oggName);
              File oldChannelContent = new File(assetLocation +
                  oggName);
              if (newChannelContent.exists()) {
                System.out.println("Moving " + oggName + " to " + oldChannelContent.getAbsolutePath());
                if (oldChannelContent.exists()) {
                  System.out.println("Deleting old " + oggName);
                  oldChannelContent.delete();
                } else {
                  System.out.println("Old " + oggName + " does not exist" + oldChannelContent.getAbsolutePath());
                }
                newChannelContent.renameTo(oldChannelContent);
                // play the sound using minecraft's sound system
                context.client().getSoundManager().play(
                    new NarratorSoundInstance(narratorEntity, soundEvent));

              } else {
                System.err.println("Move failed: " + oggName + " does not exist");
              }

            } catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          
        });

    for (int i = 0; i < 4; i++) {
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
          if (System.currentTimeMillis() > nextAllowedBlockUse) {
            // Log the block interaction
            NarratorTestClient.sendLogSuccessMessage("You interacted with a block: "
                + Registries.BLOCK.getEntry(interactedBlock).getIdAsString() + " at " + interactedPos.toShortString(),
                player);
            NarratorTestClient.addEvent(player, "Interact Block", Registries.BLOCK.getEntry(interactedBlock).getIdAsString(),
                System.currentTimeMillis());
            nextAllowedBlockUse = System.currentTimeMillis() + 150;
          } else
            NarratorTestClient.sendLogFailMessage("Remaining block interaction cooldown: "
                + Long.toString(nextAllowedBlockUse - System.currentTimeMillis()), player);
        } else {
          // Check if the player is placing a block
          ItemStack stack = player.getStackInHand(hand);
          if (stack.getItem() instanceof BlockItem) {
            if (System.currentTimeMillis() > nextAllowedBlockPlace) {
              BlockPos placementPos = hitResult.getBlockPos().offset(hitResult.getSide());
              NarratorTestClient.sendLogSuccessMessage("You placed a block: " + stack.getRegistryEntry().getIdAsString()
                  + " at " + placementPos.toShortString(), player);
              NarratorTestClient.addEvent(player, "Place Block", stack.getRegistryEntry().getIdAsString(),
                  System.currentTimeMillis());
              nextAllowedBlockPlace = System.currentTimeMillis() + 190;
            } else
              NarratorTestClient.sendLogFailMessage("Remaining block place cooldown: "
                  + Long.toString(nextAllowedBlockPlace - System.currentTimeMillis()), player);
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
            NarratorTestClient.sendLogSuccessMessage("You equipped: " + stack.getRegistryEntry().getIdAsString(), player);
            NarratorTestClient.addEvent(player, "Equip", stack.getRegistryEntry().getIdAsString(),
                System.currentTimeMillis());

          } else if (stack.isEmpty() && !armorState.get(i).isEmpty()) {
            NarratorTestClient.sendLogSuccessMessage("You unequipped: " + armorState.get(i), player);
            NarratorTestClient.addEvent(player, "Unequip", armorState.get(i), System.currentTimeMillis());
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
          if (System.currentTimeMillis() > nextAllowedBreed) {
            NarratorTestClient
                .sendLogSuccessMessage("You bred: " + Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString()
                    + " using " + heldItem.getName().getString(), player);
            NarratorTestClient.addEvent(player, "Bred", Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString()
                + " using " + heldItem.getName().getString(), System.currentTimeMillis());
            nextAllowedBreed = System.currentTimeMillis() + 300;
          } else
            NarratorTestClient.sendLogFailMessage(
                "Remaining breed cooldown: " + Long.toString(nextAllowedBreed - System.currentTimeMillis()), player);

        } else {
          NarratorTestClient.sendLogFailMessage("You tried to breed: "
              + Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString() + " with an invalid item", player);
        }
      }
      if (entity instanceof net.minecraft.entity.passive.VillagerEntity) {
        if (System.currentTimeMillis() > nextAllowedTrade) {
          NarratorTestClient.sendLogSuccessMessage(
              "You traded with : " + Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString(), player);
          NarratorTestClient.addEvent(player, "Traded with",
              Registries.ENTITY_TYPE.getEntry(entity.getType()).getIdAsString(), System.currentTimeMillis());
          nextAllowedTrade = System.currentTimeMillis() + 500;
        } else
          NarratorTestClient.sendLogFailMessage(
              "Remaining trade cooldown: " + Long.toString(nextAllowedTrade - System.currentTimeMillis()), player);

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

  public static void addEvent(PlayerEntity player, String type, String extra, long time) {
      ClientPlayNetworking.send(new EventPayload(type, extra, time));
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(NarratorTest.MOD_ID);

  public static void sendLogSuccessMessage(String message, PlayerEntity player) {
      if (ModConfig.getConfig().debugLevel == 2) {
          player.sendMessage(Text.literal("[LOGGED] " + message), false);
      }
      if (ModConfig.getConfig().debugLevel == 1) {
          LOGGER.info(message);
      }
  }

  public static void sendLogFailMessage(String message, PlayerEntity player) {
      if (ModConfig.getConfig().debugLevel == 2) {
          player.sendMessage(Text.literal("[NOT LOGGED] " + message), false);
      }
      if (ModConfig.getConfig().debugLevel == 1) {
          LOGGER.info(message);
      }
  }

  public static void sendRequestInfoMessage(String message, PlayerEntity player) {
      if (ModConfig.getConfig().debugLevel == 2) {
          player.sendMessage(Text.literal("[REQUEST] " + message), false);
      }
      if (ModConfig.getConfig().debugLevel == 1) {
          LOGGER.info(message);
      }
  }
}