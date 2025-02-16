package bbw.narratortest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import bbw.narratortest.config.ModConfig;

public class AutoFeedbackRunner {
    private static long lastRan = System.currentTimeMillis();
    public static boolean request_in_progress = false;

    public static void runMaybe(PlayerEntity player, World world){
        if (request_in_progress){
            NarratorTest.sendRequestInfoMessage("Narration request already in progress", player);
            return;
        }

        //if a lot happened
        if (NarratorTest.getLogger(player).getNumEvents() > ModConfig.getConfig().maxEvents){
            NarratorTest.sendRequestInfoMessage("Narration request for QUANTITY sending", player);
            GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).collapseEvents(), player, world);
            lastRan = System.currentTimeMillis();
        } else {
            NarratorTest.sendRequestInfoMessage("Not sent: Event " + NarratorTest.getLogger(player).getNumEvents() + "/" +  ModConfig.getConfig().maxEvents, player);
        }

        //if its time
        if (lastRan != -1 && (System.currentTimeMillis() > lastRan + (ModConfig.getConfig().narratorCooldown *1000))){
            NarratorTest.sendRequestInfoMessage("Narration request for TIME sending", player);
            GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).dontCollapseEvents(), player, world);
            lastRan = System.currentTimeMillis();
        } else {
            NarratorTest.sendRequestInfoMessage("Not sent: Time " + ((System.currentTimeMillis()-lastRan)/1000) + "/" + (ModConfig.getConfig().narratorCooldown), player);
        }
    }

    public static void runDefinitely(PlayerEntity player, World world){
        if (request_in_progress){
            NarratorTest.sendRequestInfoMessage("Narration request already in progress", player);
            return;
        }
        NarratorTest.sendRequestInfoMessage("Narration request for IMPORTANT sending", player);
        GTPInterface.getGPTFeedback(NarratorTest.getLogger(player).dontCollapseEvents(), player, world);
        lastRan = System.currentTimeMillis();
    }
}
