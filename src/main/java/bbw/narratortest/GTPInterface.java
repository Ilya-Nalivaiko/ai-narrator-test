package bbw.narratortest;

import java.util.concurrent.CompletableFuture;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class GTPInterface {
    public static void getGPTFeedback(String prompt, PlayerEntity player, World world){
        AutoFeedbackRunner.request_in_progress = true;
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

                TTSGenerator.speak(narration, player, world);
                AutoFeedbackRunner.request_in_progress = false;
            }
        }).exceptionally(ex -> {
            System.out.println("[ERROR] Error in GPT request: " + ex.getMessage());
            AutoFeedbackRunner.request_in_progress = false;
            ex.printStackTrace();
            return null;
        });
    }
}
