package bbw.narratortest;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CustomSounds {

	private CustomSounds() {
		// private empty constructor to avoid accidental instantiation
	}

	public static final SoundEvent STEEL_PIPE_CRASH = registerSound("steel_pipe_crash");

	// actual registration of all the custom SoundEvents
	private static SoundEvent registerSound(String id) {
		Identifier identifier = Identifier.of(NarratorTest.MOD_ID, id);
		return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
	}

	// This static method starts class initialization, which then initializes
	// the static class variables (e.g. ITEM_METAL_WHISTLE).
	public static void initialize() {
		NarratorTest.LOGGER.info("Registering " + NarratorTest.MOD_ID + " Sounds");
		// Technically this method can stay empty, but some developers like to notify
		// the console, that certain parts of the mod have been successfully initialized
	}



	private boolean hasInitialized = false;
	public void liveInit(){
		if (!hasInitialized){
			initialize();
			hasInitialized = true;
		}
	}
}
