package bbw.narratortest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class AutoFeedbackRunner {
    private static long lastRan = -1;
    private static final int COOLDOWN = 20*1000;
    private static final int MAX_EVENTS = 40;
    public static boolean request_in_progress = false;

    public static void runMaybe(PlayerEntity player, World world){
        if (request_in_progress){
            NarratorTest.sendLogFailMessage("Narration request already in progress", player);
            return;
        }

        //if a lot happened
        if (NarratorTest.getLogger(player).getNumEvents() > MAX_EVENTS){
            GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).collapseEvents(), player, world);
            lastRan = System.currentTimeMillis();
        }

        //if its time
        if (lastRan != -1 && (System.currentTimeMillis() > lastRan+COOLDOWN)){
            GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).dontCollapseEvents(), player, world);
            lastRan = System.currentTimeMillis();
        }
    }

    public static void runDefinitely(PlayerEntity player, World world){
        if (request_in_progress){
            NarratorTest.sendLogFailMessage("Narration request already in progress", player);
            return;
        }
        GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).dontCollapseEvents(), player, world);
        lastRan = System.currentTimeMillis();
    }
}
