package bbw.narratortest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.mojang.authlib.GameProfile;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import com.mojang.authlib.properties.Property;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TTSGenerator {
  private TTSGenerator() {
    // private empty constructor to avoid accidental instantiation
  }

  // registers the TTS voice
  public static void init() {
    System.setProperty("freetts.voices",
        "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
  }

  public static void speak(String text, PlayerEntity player, World world) {
    if (world.isClient) {
      System.out.println("Cannot generate TTS audio on client side.");
      return;
    }
    
    // Pass whichever head item you want
    ItemStack head = new ItemStack(Items.PLAYER_HEAD);
    GameProfile targetProfile = player.getGameProfile();

    Property textureProperty = new Property("textures",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDBmMjU5ZDJhNTkxMTVlNjJiYjllOWY4NmNlZTg4OTA5YmVkMDI5NGMzYTA4ZGJiNWRmMzlmMmYyNjJkMDhhNSJ9fX0=");
    targetProfile.getProperties().put("textures", textureProperty);
    head.set(DataComponentTypes.PROFILE, new ProfileComponent(targetProfile));
    ArmorStandEntity armorStand = spawnNarratorArmorStand(player, world, head);

    // Optionally despawn it after 5 seconds
    if (armorStand != null) {
      despawnNarratorArmorStand(armorStand, 20);
    }

    // ✅ Text-to-Speech Processing
    VoiceManager voiceManager = VoiceManager.getInstance();
    Voice voice = voiceManager.getVoice("kevin16");

    if (voice == null) {
      System.out.println("[ERROR] Voice 'kevin16' not found.");
      return;
    }

    voice.allocate();
    ByteArrayAudioPlayer audioPlayer = new ByteArrayAudioPlayer(AudioFileFormat.Type.WAVE);
    voice.setAudioPlayer(audioPlayer);
    voice.speak(text);
    voice.deallocate();

    System.out.println("DONE GENERATING AUDIO");

    byte[] audioBytes = audioPlayer.getAudioBytes();
    System.out.println("AUDIO BYTES: " + audioBytes.length);
    System.out.println("Format: " + audioPlayer.getAudioFormat());

    try {
      ByteArrayOutputStream withHeader = new ByteArrayOutputStream();
      AudioSystem.write(
          new AudioInputStream(
              new ByteArrayInputStream(audioBytes),
              audioPlayer.getAudioFormat(),
              audioBytes.length),
          audioPlayer.targetType,
          withHeader);
      byte[] finalBytes = withHeader.toByteArray();
      // Send to nearby players
      world.getPlayers().stream()
          .filter(p -> p.getBlockPos().isWithinDistance(player.getBlockPos(), 20))
          .forEach(nearbyPlayer -> {
            System.out.println("Sending audio to player: " +
                nearbyPlayer.getName().getString());
            ServerPlayNetworking.send((ServerPlayerEntity) nearbyPlayer,
                new TtsPayload(player.getBlockPos(), finalBytes));
          });

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static ArmorStandEntity spawnNarratorArmorStand(PlayerEntity player, World world, ItemStack headItemStack) {
    if (!(player instanceof ServerPlayerEntity))
      return null;

    Vec3d playerPos = player.getPos();
    ArmorStandEntity armorStand = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
    // Spawn 1.5 blocks above the player to avoid ground collision.
    armorStand.refreshPositionAndAngles(playerPos.x, playerPos.y + 0.5, playerPos.z, 0, 0);
    armorStand.setCustomName(Text.literal("Backseat"));
    armorStand.setCustomNameVisible(true);
    armorStand.setInvisible(true); // Change to true if you want it invisible
    armorStand.setNoGravity(true); // Let it float

    // Equip the Armor Stand with the chosen head (or default to Creeper Head)
    if (headItemStack != null) {
      armorStand.equipStack(EquipmentSlot.HEAD, headItemStack);
    } else {
      armorStand.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.CREEPER_HEAD));
    }

    world.spawnEntity(armorStand);
    System.out.println("✅ Spawned Armor Stand for " + player.getName().getString());

    // Register a Fabric tick event to update the armor stand each server tick.
    ServerTickEvents.START_SERVER_TICK.register(server -> {
      // If the armor stand is removed or the player is dead, skip updates.
      if (armorStand.isRemoved() || !player.isAlive()) {
        // Note: Fabric's tick events don't support easy unregistration,
        // so this callback will simply do nothing once these conditions are met.
        return;
      }

      // Make the armor stand spin by increasing its yaw.
      float currentYaw = armorStand.getYaw();
      armorStand.setYaw(currentYaw + 5.0f); // Adjust spin speed as desired

      // Have the armor stand follow the player.
      Vec3d targetPos = player.getPos().add(0, 1, 0); // Target position above the player.
      Vec3d currentPos = armorStand.getPos();
      Vec3d diff = targetPos.subtract(currentPos);
      double distance = diff.length();

      // If the armor stand is too far from the target, move it closer.
      if (distance > 2.0) {
        Vec3d movement = diff.normalize().multiply(0.1); // Adjust movement speed as needed
        armorStand.setPosition(
            currentPos.x + movement.x,
            currentPos.y + movement.y,
            currentPos.z + movement.z);
      }
    });

    return armorStand;
  }

  private static void despawnNarratorArmorStand(ArmorStandEntity armorStand, int delaySeconds) {
    if (armorStand == null || armorStand.isRemoved())
      return;

    // Run only on server side
    if (!armorStand.getEntityWorld().isClient()) {
      CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS).execute(() -> {
        if (!armorStand.isRemoved()) {
          armorStand.discard(); // Properly removes the entity
          System.out.println("❌ Despawned Armor Stand: " + armorStand.getCustomName());
        }
      });
    }
  }

}