package bbw.narratortest;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bbw.narratortest.config.ModConfig;
import bbw.narratortest.event.AdvancementDetectionHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;


public class NarratorTest implements ModInitializer {
	private static HashMap<String, EventLogger> eventLoggerMap = new HashMap<>();

    public static final String MOD_ID = "narrator-test";

    public static long startTime = 0;

    // Logger for console and log file
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void addEvent(PlayerEntity player, String type, String extra, long time){
		EventLogger logger = eventLoggerMap.get(player.getUuidAsString());
		if (logger == null){
			logger = new EventLogger();
			eventLoggerMap.put(player.getUuidAsString(), logger);
		}
		logger.appendEvent(type, extra, time);
		if (type == "Died" || type == "Advancment Made"){
			AutoFeedbackRunner.runDefinitely(player, player.getWorld());
		} else {
			AutoFeedbackRunner.runMaybe(player, player.getWorld());
		}
		
	}

	public static EventLogger getLogger(PlayerEntity player){
		EventLogger logger = eventLoggerMap.get(player.getUuidAsString());
		if (logger == null){
			logger = new EventLogger();
			eventLoggerMap.put(player.getUuidAsString(), logger);
		}
		return logger;
	}

	public static void clearLoggers(){
		eventLoggerMap = new HashMap<>();
	}

	@Override
	public void onInitialize() {

        ServerMessageEvents.GAME_MESSAGE.register(new AdvancementDetectionHandler());


		// This code runs as soon as Minecraft is in a mod-load-ready state.
        LOGGER.info("Hello Fabric world!");

        startTime = System.currentTimeMillis();

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

		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		System.setProperty("freetts.voices",
				"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");


		// UseItemCallback.EVENT.register((player, world, hand) -> {
		// 	if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
		// 		VoiceManager voiceManager = VoiceManager.getInstance();
		// 		Voice voice = voiceManager.getVoice("kevin16");
		// 		voice.allocate();
		// 		ByteArrayAudioPlayer audioPlayer = new ByteArrayAudioPlayer(AudioFileFormat.Type.WAVE);
		// 		voice.setAudioPlayer(audioPlayer);
		// 		voice.speak("This is a test of the text to speech capabilities");
		// 		voice.deallocate();

		// 		System.out.println("DONE GENERATING AUDIO");

		// 		byte[] audioBytes = audioPlayer.getAudioBytes();
		// 		System.out.println("AUDIO BYTES: " + audioBytes.length);
		// 		System.out.println("Format:" + audioPlayer.getAudioFormat());

		// 		try {
		// 			ByteArrayOutputStream withHeader = new ByteArrayOutputStream();
		// 			AudioSystem.write(
		// 					new AudioInputStream(
		// 							new ByteArrayInputStream(audioBytes),
		// 							audioPlayer.getAudioFormat(),
		// 							audioBytes.length),
		// 					audioPlayer.targetType,
		// 					withHeader);
		// 			byte[] finalBytes = withHeader.toByteArray();
					// // Send to nearby players
					// world.getPlayers().stream()
					// 		.filter(p -> p.getBlockPos().isWithinDistance(player.getBlockPos(), 20))
					// 		.forEach(nearbyPlayer -> {
					// 			System.out.println("Sending audio to player: " +
					// 					nearbyPlayer.getName().getString());
					// 			ServerPlayNetworking.send((ServerPlayerEntity) nearbyPlayer,
					// 					new TtsPayload(player.getBlockPos(), finalBytes));
					// 		});

		// 		} catch (Exception e) {
		// 			e.printStackTrace();
		// 		}

		// 	}
		// 	return ActionResult.PASS;
		// });
	}

    public static void sendLogSuccessMessage(String message, PlayerEntity player){
        if (ModConfig.getConfig().debugLevel == 2){
            player.sendMessage(Text.literal("[LOGGED] " + message), false);
        }
        if (ModConfig.getConfig().debugLevel == 1){
            LOGGER.info(message);
        }
    }

    public static void sendLogFailMessage(String message, PlayerEntity player){
        if (ModConfig.getConfig().debugLevel == 2){
            player.sendMessage(Text.literal("[NOT LOGGED] " + message), false);
        }
        if (ModConfig.getConfig().debugLevel == 1){
            LOGGER.info(message);
        }
    }

	public static void sendRequestInfoMessage(String message, PlayerEntity player){
        if (ModConfig.getConfig().debugLevel == 2){
            player.sendMessage(Text.literal("[REQUEST] " + message), false);
        }
        if (ModConfig.getConfig().debugLevel == 1){
            LOGGER.info(message);
        }
    }

}

 
