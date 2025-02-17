package bbw.narratortest;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CustomSounds {

	// private constructor to prevent instantiation
	private CustomSounds() {
	}

	public static final SoundEvent NARRATION_CHANNEL_0 = registerSound("narration_channel0");
	public static final SoundEvent NARRATION_CHANNEL_1 = registerSound("narration_channel1");
	public static final SoundEvent NARRATION_CHANNEL_2 = registerSound("narration_channel2");
	public static final SoundEvent NARRATION_CHANNEL_3 = registerSound("narration_channel3");
	public static final SoundEvent NARRATION_CHANNEL_4 = registerSound("narration_channel4");
	public static final SoundEvent NARRATION_CHANNEL_5 = registerSound("narration_channel5");
	public static final SoundEvent NARRATION_CHANNEL_6 = registerSound("narration_channel6");

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

	public static final SoundEvent getChannel(int channel) {
		switch (channel) {
			case 0:
				return NARRATION_CHANNEL_0;
			case 1:
				return NARRATION_CHANNEL_1;
			case 2:
				return NARRATION_CHANNEL_2;
			case 3:
				return NARRATION_CHANNEL_3;
			case 4:
				return NARRATION_CHANNEL_4;
			case 5:
				return NARRATION_CHANNEL_5;
			case 6:
				return NARRATION_CHANNEL_6;
			default:
				return NARRATION_CHANNEL_0;
		}
	}

}
