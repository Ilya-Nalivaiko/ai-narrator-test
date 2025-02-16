package bbw.narratortest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarratorTest implements ModInitializer {
	public static final String MOD_ID = "narrator-test";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		System.setProperty("freetts.voices",
				"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
				VoiceManager voiceManager = VoiceManager.getInstance();
				Voice voice = voiceManager.getVoice("kevin16");
				voice.allocate();
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ByteArrayAudioPlayer audioPlayer = new ByteArrayAudioPlayer(byteArrayOutputStream);
				voice.setAudioPlayer(audioPlayer);
				voice.speak("This is a test of the text to speech capabilities");
				voice.deallocate();

				System.out.println("DONE GENERATING AUDIO");

				byte[] audioBytes = byteArrayOutputStream.toByteArray();

				System.out.println("AUDIO BYTES: " + audioBytes.length);
				System.out.println("Format:" + audioPlayer.getAudioFormat());

				try {
				

					// Wrap the byte buffer in a stream and create an AudioInputStream
					ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
					AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
					AudioInputStream ais = new AudioInputStream(bais, format, audioBytes.length);

					// Obtain a Clip, open it, and play
					Clip clip = AudioSystem.getClip();
					clip.open(ais);
					clip.start();

					// Optionally, wait for the clip to finish playing
					Thread.sleep((long) ((audioBytes.length / 16000) * 1000));
				} catch (Exception e) {
					e.printStackTrace();
				}

				// // Send to nearby players
				// world.getPlayers().stream()
				// .filter(p -> p.getBlockPos().isWithinDistance(player.getBlockPos(), 20))
				// .forEach(nearbyPlayer -> {
				// System.out.println("Sending audio to player: " +
				// nearbyPlayer.getName().getString());
				// ServerPlayNetworking.send((ServerPlayerEntity) nearbyPlayer,
				// new TtsPayload(player.getBlockPos(), audioBytes));
				// });
			}
			return ActionResult.PASS;
		});
	}
}
