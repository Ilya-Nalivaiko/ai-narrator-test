package bbw.narratortest;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class AutoFeedbackRunner {
    private static long lastRan = -1;
    private static int cooldown = 20;

    public static void runMaybe(ServerPlayerEntity player, World world){
        //if a lot happened
        if (NarratorTest.getLogger(player).getNumEvents() > 40){
            GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).collapseEvents(), player, world);
            lastRan = System.currentTimeMillis();
        }

        //if its time
        if (lastRan != -1 && (System.currentTimeMillis() > lastRan+cooldown)){
            GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).dontCollapseEvents(), player, world);
            lastRan = System.currentTimeMillis();
        }
    }

    public static void runDefinitely(ServerPlayerEntity player, World world){
        GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).dontCollapseEvents(), player, world);
        lastRan = System.currentTimeMillis();
    }
}
