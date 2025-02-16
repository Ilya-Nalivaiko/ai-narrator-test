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
				ByteArrayAudioPlayer audioPlayer = new ByteArrayAudioPlayer(AudioFileFormat.Type.WAVE);
				voice.setAudioPlayer(audioPlayer);
				voice.speak("This is a test of the text to speech capabilities");
				voice.deallocate();

				System.out.println("DONE GENERATING AUDIO");

				byte[] audioBytes = audioPlayer.getAudioBytes();
				System.out.println("AUDIO BYTES: " + audioBytes.length);
				System.out.println("Format:" + audioPlayer.getAudioFormat());

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
			return ActionResult.PASS;
		});
	}
}
