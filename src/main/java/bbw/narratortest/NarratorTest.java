package bbw.narratortest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bbw.narratortest.config.ModConfig;
import bbw.narratortest.event.AdvancementDetectionHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;


public class NarratorTest implements ModInitializer {
	public static EventLogger eventLogger = new EventLogger();

    public static final String MOD_ID = "narrator-test";

    public static long startTime = 0;

    // Logger for console and log file
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        
        ServerMessageEvents.GAME_MESSAGE.register(new AdvancementDetectionHandler());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            NarratorConfigCommand.register(dispatcher);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DebugCommand.register(dispatcher);
        });
        
        // Register entity death event
        ServerLivingEntityEvents.AFTER_DEATH.register((LivingEntity entity, DamageSource damageSource) -> {
            if (damageSource.getAttacker() instanceof PlayerEntity) {
                // If the entity was killed by a player, call the onEntityKill method
                PlayerEntity player = (PlayerEntity) damageSource.getAttacker();
                ServerEventCalls.onEntityKill(player, (ServerWorld) entity.getWorld(), entity, damageSource);
            }
        });

        // Register server tick event for biome and structure detection
        ServerTickEvents.END_WORLD_TICK.register(ServerEventCalls::onWorldTick);

        // Register block break event
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            ServerEventCalls.onBlockBreak((ServerPlayerEntity) player, pos, state.getBlock().getName().getString());
        });

    }

    public static void sendDebugMessage(String message, PlayerEntity player){
        if (ModConfig.getConfig().debugLevel == 2){
            player.sendMessage(Text.literal("[DEBUG] " + message), false);
        }
        if (ModConfig.getConfig().debugLevel >= 1){
            LOGGER.debug(message);
        }
    }
}