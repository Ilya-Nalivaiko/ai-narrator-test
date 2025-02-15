package bbw.narratortest;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class GTPInterface {
    public static void getGPTFeedback(String prompt, ClientPlayerEntity player){
        // Print to console for debugging
        System.out.println("[DEBUG] Sending request to ChatGPT: " + prompt);

        // Run GPT request asynchronously
        CompletableFuture.supplyAsync(() -> {
            String narration = ChatGPTTest.getNarration(prompt);
            
            // Debugging: Print GPT response to console
            System.out.println("[DEBUG] GPT Response: " + narration);

            return narration;
        }).thenAccept(narration -> {
            if (narration == null || narration.isEmpty()) {
                System.out.println("[ERROR] GPT returned an empty response.");
            } else {
                // Send the GPT-generated narration to the player
                player.sendMessage(Text.literal(narration), false);
            }
        }).exceptionally(ex -> {
            System.out.println("[ERROR] Error in GPT request: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
    }
}
