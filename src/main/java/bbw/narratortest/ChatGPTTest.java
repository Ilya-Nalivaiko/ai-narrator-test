package bbw.narratortest;

import org.json.JSONObject;
import okhttp3.*;

public class ChatGPTTest {
    // Load API key from environment variable
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

    // Debug: Print API key status
    static {
        System.out.println("[DEBUG] OPENAI_API_KEY: " + (OPENAI_API_KEY == null ? "NULL" : "Loaded Successfully"));
    }

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build();

    public static String getNarration(String input) {
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            return "Error: Missing OpenAI API key.";
        }

        JSONObject json = new JSONObject();
        json.put("model", "gpt-4");
        json.put("temperature", 0.3); 
        json.put("messages", new Object[]{
                new JSONObject().put("role", "system").put("content", "You are a backseat gaming minecraft player. You are very opiniated and mean. Keep it max 2 sentences, unless the player really makes a mistake like making a diamond hoe."),
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

            return new JSONObject(response.body().string())
                    .getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content");

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("[ERROR] Timeout occurred.");
            return "Error: OpenAI request timed out.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to communicate with OpenAI.";
        }
    }
}