package bbw.narratortest;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.lwjgl.openal.AL10;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class NarratorTestClient implements ClientModInitializer {

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
            // Read the position (if needed)
            BlockPos pos = payload.pos();
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

    // System.out.println("Received TTS payload");
    // ClientPlayerEntity player = MinecraftClient.getInstance().player;
    // if (player == null)
    // return;
    // // Execute on render thread
    // context.client().execute(() -> {
    // try {
    // byte[] audioData = payload.audioData();
    // BlockPos pos = payload.pos();
    // System.out.println("Playing audio at " + pos);

    // // Simple WAV header parsing (for 16-bit PCM)
    // int channels = (audioData[22] & 0xFF);
    // int sampleRate = (audioData[24] & 0xFF) |
    // (audioData[25] << 8) |
    // (audioData[26] << 16) |
    // (audioData[27] << 24);

    // int dataStart = 44; // Skip WAV header
    // byte[] pcmData = Arrays.copyOfRange(audioData, dataStart, audioData.length);
    // ByteBuffer directBuffer = ByteBuffer.allocateDirect(pcmData.length);
    // directBuffer.put(pcmData);
    // directBuffer.flip();
    // System.out.println("Channels: " + channels + ", Sample rate: " + sampleRate
    // + ", Data length: " + pcmData.length);

    // // Create OpenAL buffer
    // int format = getOpenALFormat(channels, 16);
    // int buffer = AL10.alGenBuffers();
    // System.out.println("Format: " + format);

    // AL10.alBufferData(buffer, format, directBuffer, sampleRate);

    // System.out.println("Buffer: " + buffer);

    // // Create and configure source
    // int source = AL10.alGenSources();
    // AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
    // AL10.alSource3f(source, AL10.AL_POSITION,
    // pos.getX() + 0.5f,
    // pos.getY() + 0.5f,
    // pos.getZ() + 0.5f);
    // AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, 5.0f);
    // AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, 20.0f);
    // AL10.alSourcePlay(source);

    // System.out.println("Source: " + source);

    // // Schedule resource cleanup
    // context.client().submit(() -> {
    // if (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_STOPPED) {
    // AL10.alDeleteSources(source);
    // AL10.alDeleteBuffers(buffer);
    // }
    // });

    // System.out.println("Playing audio");
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // });
    // });
  }

  private static int getOpenALFormat(int channels, int bitsPerSample) {
    if (channels == 1) {
      return bitsPerSample == 8 ? AL10.AL_FORMAT_MONO8 : AL10.AL_FORMAT_MONO16;
    } else {
      return bitsPerSample == 8 ? AL10.AL_FORMAT_STEREO8 : AL10.AL_FORMAT_STEREO16;
    }
  }
}