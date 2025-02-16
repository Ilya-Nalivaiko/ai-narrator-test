package bbw.narratortest.event;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bbw.narratortest.NarratorTest;

public class AdvancementDetectionHandler implements ServerMessageEvents.GameMessage {

    // Regex to match advancement messages
    private static final Pattern ADVANCEMENT_PATTERN = Pattern.compile("(.+) has made the advancement \\[(.+)]");

    @Override
    public void onGameMessage(MinecraftServer server, Text message, boolean overlay) {
        // Convert the message to a string
        String messageText = message.getString();

        // Match the message against the advancement pattern
        Matcher matcher = ADVANCEMENT_PATTERN.matcher(messageText);
        if (matcher.matches()) {
            // Extract the player name and advancement name
            String playerName = matcher.group(1);
            String advancementName = matcher.group(2);

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);

            NarratorTest.sendDebugMessage("You earned the advancement: " + advancementName, player);
            NarratorTest.eventLogger.appendEvent("Advancment Made", advancementName, System.currentTimeMillis());
        }
    }
}