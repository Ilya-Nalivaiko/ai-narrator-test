package bbw.narratortest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class NarratorTest implements ModInitializer {
    public static final String MOD_ID = "narrator-test";

    // Logger for console and log file
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Map to store the last biome of each player
    private static final Map<ServerPlayerEntity, RegistryKey<Biome>> playerBiomeMap = new HashMap<>();

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

        // Register server tick event for biome detection
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
    }

    // Called on each world tick
    private void onWorldTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            // Get the player's current biome
            BlockPos playerPos = player.getBlockPos();
            RegistryKey<Biome> currentBiomeKey = world.getBiome(playerPos).getKey().orElse(null);

            // Get the player's last biome from the map
            RegistryKey<Biome> lastBiomeKey = playerBiomeMap.get(player);

            // Check if the biome has changed
            if (currentBiomeKey != null && !currentBiomeKey.equals(lastBiomeKey)) {
                // Biome has changed
                onBiomeChange(player, currentBiomeKey);

                // Update the player's last biome in the map
                playerBiomeMap.put(player, currentBiomeKey);
            }
        }
    }

    // Handles biome change events
    private void onBiomeChange(ServerPlayerEntity player, RegistryKey<Biome> biomeKey) {
        // Get the biome's name
        Identifier biomeId = biomeKey.getValue();
        String biomeName = biomeId.getPath(); // e.g., "plains", "desert"

        // Send a message to the player
        player.sendMessage(Text.literal("You entered the " + biomeName + " biome!"), false);
    }

    // Handles killing entities (server-side)
    private void onEntityKill(PlayerEntity player, ServerWorld world, LivingEntity killedEntity, DamageSource source) {
        player.sendMessage(Text.literal("You just killed: " + killedEntity.getName().getString()), false);
    }
}