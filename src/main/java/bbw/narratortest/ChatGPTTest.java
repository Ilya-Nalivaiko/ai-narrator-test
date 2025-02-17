package bbw.narratortest;

import bbw.narratortest.config.ModConfig;
import org.json.JSONObject;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

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

        systemPrompt += " IMPORTANT: keep it to a few sentences max";

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
