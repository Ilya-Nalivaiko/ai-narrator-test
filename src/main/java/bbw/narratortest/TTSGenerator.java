package bbw.narratortest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;


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

    if (!world.isClient) {
      // Pass whichever head item you want
      ArmorStandEntity armorStand = spawnNarratorArmorStand(player, world, Items.CREEPER_HEAD);

      // Optionally despawn it after 5 seconds
      if (armorStand != null) {
          despawnNarratorArmorStand(armorStand, 20);
      }
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
  
  private static ArmorStandEntity spawnNarratorArmorStand(PlayerEntity player, World world, Item headItem) {
    if (!(player instanceof ServerPlayerEntity)) return null;

    Vec3d playerPos = player.getPos();
    ArmorStandEntity armorStand = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
    armorStand.refreshPositionAndAngles(playerPos.x, playerPos.y, playerPos.z, 0, 0);
    armorStand.setCustomName(player.getName());
    armorStand.setCustomNameVisible(true);
    armorStand.setInvisible(false); // Set to `true` if you want it invisible
    armorStand.setNoGravity(true); // Makes it float in place

    // Equip the Armor Stand with the chosen mob head, e.g. Items.ZOMBIE_HEAD or Items.SKELETON_SKULL
    if (headItem != null) {
        armorStand.equipStack(EquipmentSlot.HEAD, new ItemStack(headItem));
    } else {
        // Fallback if headItem was null
        armorStand.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.CREEPER_HEAD));
    }

    world.spawnEntity(armorStand);
    System.out.println("✅ Spawned Armor Stand for " + player.getName().getString());
    return armorStand;
}



  
  private static void despawnNarratorArmorStand(ArmorStandEntity armorStand, int delaySeconds) {
      if (armorStand == null || armorStand.isRemoved()) return;
  
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