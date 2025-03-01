package bbw.narratortest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
public class ServerEventCalls {
    // Map to store the last biome of each player
    private static final Map<ServerPlayerEntity, RegistryKey<Biome>> playerBiomeMap = new HashMap<>();

    // Map to store the last structure of each player
    private static final Map<ServerPlayerEntity, Identifier> playerStructureMap = new HashMap<>();


    // Called on each world tick
    public static void onWorldTick(ServerWorld world) {
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
    private static void onBiomeChange(ServerPlayerEntity player, RegistryKey<Biome> biomeKey) {
        Identifier biomeId = biomeKey.getValue();
        String biomeName = biomeId.getPath(); // e.g., "plains", "desert"
        NarratorTest.sendLogSuccessMessage("You entered the " + biomeId.getNamespace()+":"+biomeName + " biome!", player);
		NarratorTest.addEvent(player, "Enter Biome", biomeId.getNamespace()+":"+biomeName, System.currentTimeMillis());
    }

    // Handles structure enter events
    private static void onStructureEnter(ServerPlayerEntity player, Identifier structureId) {
        String structureName = structureId.getPath(); // e.g., "village", "stronghold"
        NarratorTest.sendLogSuccessMessage("You entered a " + structureId.getNamespace()+":"+structureName + "!", player);
		NarratorTest.addEvent(player, "Enter Structure", structureId.getNamespace()+":"+structureName, System.currentTimeMillis());
    }

    // Handles killing entities (server-side)
    public static void onEntityKill(PlayerEntity player, ServerWorld world, LivingEntity killedEntity, DamageSource source) {
        NarratorTest.sendLogSuccessMessage("You just killed: " + Registries.ENTITY_TYPE.getEntry(killedEntity.getType()).getIdAsString() + " with " + player.getMainHandStack().getRegistryEntry().getIdAsString(), player);
        NarratorTest.addEvent(player, "Kill Entity", Registries.ENTITY_TYPE.getEntry(killedEntity.getType()).getIdAsString() + " with " + player.getMainHandStack().getRegistryEntry().getIdAsString(), System.currentTimeMillis());
    }

    // Handles block breaking events
    public static void onBlockBreak(ServerPlayerEntity player, BlockPos pos, String blockName) {
        ItemStack heldItem = player.getMainHandStack();
        String toolName = heldItem.isEmpty() ? "hands" : heldItem.getRegistryEntry().getIdAsString();

        NarratorTest.sendLogSuccessMessage("You broke a block: " + blockName + " with " + toolName, player);
        NarratorTest.addEvent(player, "Break Block", blockName + " with " + toolName, System.currentTimeMillis());
    }

    // Gets the structure at a specific position
    private static Identifier getStructureAt(ServerWorld world, BlockPos pos) {
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
