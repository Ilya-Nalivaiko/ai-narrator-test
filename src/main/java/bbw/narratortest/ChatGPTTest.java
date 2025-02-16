package bbw.narratortest;

import bbw.narratortest.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
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

public class ChatGPTTest {
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

    static {
        System.out.println("[DEBUG] OPENAI_API_KEY: " + (OPENAI_API_KEY == null ? "NULL" : "Loaded Successfully"));
        ModConfig.loadConfig(); // ✅ Load config at startup

        // ✅ Setup FreeTTS
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
    }

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public static String getNarration(String input) {
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            return "Error: Missing OpenAI API key.";
        }

        // ✅ Read values from config
        double temperature = ModConfig.getConfig().temperature;
        String systemPrompt = ModConfig.getConfig().systemPrompt;

        JSONObject json = new JSONObject();
        json.put("model", "gpt-4");
        json.put("temperature", temperature);
        json.put("messages", new Object[]{
                new JSONObject().put("role", "system").put("content", systemPrompt),
                new JSONObject().put("role", "user").put("content", input)
        });

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() == null) {
                return "Error: Empty response from OpenAI.";
            }

            String narration = new JSONObject(response.body().string())
                    .getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content");

            // ✅ Text-to-Speech Processing
            VoiceManager voiceManager = VoiceManager.getInstance();
            Voice voice = voiceManager.getVoice("kevin16");

            if (voice == null) {
                System.out.println("[ERROR] Voice 'kevin16' not found.");
                return "Error: Voice synthesis failed.";
            }

            voice.allocate();
            ByteArrayAudioPlayer audioPlayer = new ByteArrayAudioPlayer(AudioFileFormat.Type.WAVE);
            voice.setAudioPlayer(audioPlayer);
            voice.speak(narration);
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
                // TODO: Insert logic to send `finalBytes` to players if necessary
                // (e.g., using ServerPlayNetworking)
                
            } catch (Exception e) {
                e.printStackTrace();
            }

            return narration;

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("[ERROR] Timeout occurred.");
            return "Error: OpenAI request timed out.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to communicate with OpenAI.";
        }
    }
}
