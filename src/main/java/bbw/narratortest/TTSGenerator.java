package bbw.narratortest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import bbw.narratortest.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.json.JSONObject;
import okhttp3.*;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TTSGenerator {
  private TTSGenerator() {
    // private empty constructor to avoid accidental instantiation
  }

  // registers the TTS voice
  public static void init() {
    System.setProperty("freetts.voices",
        "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
  }

  public static void speak(String text, ServerPlayerEntity player, World world) {
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
}