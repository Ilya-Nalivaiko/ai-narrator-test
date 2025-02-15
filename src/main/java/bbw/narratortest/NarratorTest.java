package bbw.narratortest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockBox;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class NarratorTest implements ModInitializer {
    public static final String MOD_ID = "narrator-test";

    // Logger for console and log file
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Map to store the last biome of each player
    private static final Map<ServerPlayerEntity, RegistryKey<Biome>> playerBiomeMap = new HashMap<>();

    // Map to store the last structure of each player
    private static final Map<ServerPlayerEntity, Identifier> playerStructureMap = new HashMap<>();

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

        // Register server tick event for biome and structure detection
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
    }

    // Called on each world tick
    private void onWorldTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            // Get the player's current position
            BlockPos playerPos = player.getBlockPos();

            // Biome detection
            RegistryKey<Biome> currentBiomeKey = world.getBiome(playerPos).getKey().orElse(null);
            RegistryKey<Biome> lastBiomeKey = playerBiomeMap.get(player);
            if (currentBiomeKey != null && !currentBiomeKey.equals(lastBiomeKey)) {
                onBiomeChange(player, currentBiomeKey);
                playerBiomeMap.put(player, currentBiomeKey);
            }

            // Structure detection
            Identifier currentStructureId = getStructureAt(world, playerPos);
            Identifier lastStructureId = playerStructureMap.get(player);
            if (currentStructureId != null && !currentStructureId.equals(lastStructureId)) {
                onStructureEnter(player, currentStructureId);
                playerStructureMap.put(player, currentStructureId);
            } else if (currentStructureId == null && lastStructureId != null) {
                // Player exited a structure
                playerStructureMap.put(player, null);
            }
        }
    }

    // Handles biome change events
    private void onBiomeChange(ServerPlayerEntity player, RegistryKey<Biome> biomeKey) {
        Identifier biomeId = biomeKey.getValue();
        String biomeName = biomeId.getPath(); // e.g., "plains", "desert"
        player.sendMessage(Text.literal("You entered the " + biomeName + " biome!"), false);
    }

    // Handles structure enter events
    private void onStructureEnter(ServerPlayerEntity player, Identifier structureId) {
        String structureName = structureId.getPath(); // e.g., "village", "stronghold"
        player.sendMessage(Text.literal("You entered a " + structureName + "!"), false);
    }

    // Handles killing entities (server-side)
    private void onEntityKill(PlayerEntity player, ServerWorld world, LivingEntity killedEntity, DamageSource source) {
        player.sendMessage(Text.literal("You just killed: " + killedEntity.getName().getString()), false);
    }

    // Gets the structure at a specific position
    private Identifier getStructureAt(ServerWorld world, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Predicate<Structure> predicate = structure -> true; // Match all structures

        for (StructureStart structureStart : world.getStructureAccessor().getStructureStarts(chunkPos, predicate)) {
            BlockBox boundingBox = structureStart.getBoundingBox();
            if (boundingBox.contains(pos)) {
                // Get the structure's registry key
                return world.getRegistryManager()
                        .getOptional(RegistryKeys.STRUCTURE)
                        .flatMap(registry -> registry.getKey(structureStart.getStructure()))
                        .map(RegistryKey::getValue)
                        .orElse(null);
            }
        }
        return null; // No structure found at this position
    }
}