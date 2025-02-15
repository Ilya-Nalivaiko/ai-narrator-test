package bbw.narratortest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarratorTest implements ModInitializer {
    public static final String MOD_ID = "narrator-test";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        LOGGER.info("Hello Fabric world!");

        // Register entity death event
        ServerLivingEntityEvents.AFTER_DEATH.register((LivingEntity entity, DamageSource damageSource) -> {
            if (damageSource.getAttacker() instanceof PlayerEntity) {
                // If the entity was killed by a player, call the onEntityKill method
                PlayerEntity player = (PlayerEntity) damageSource.getAttacker();
                onEntityKill(player, (ServerWorld) entity.getWorld(), entity, damageSource);
            }
        });
    }

    // Handles killing entities (server-side)
    public static void onEntityKill(PlayerEntity player, ServerWorld world, LivingEntity killedEntity, DamageSource source) {
        player.sendMessage(Text.literal("You just killed: " + killedEntity.getName().getString()), false);
    }
}