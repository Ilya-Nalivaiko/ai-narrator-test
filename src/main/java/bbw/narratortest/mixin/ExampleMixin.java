package bbw.narratortest.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bbw.narratortest.NarratorTest;
import bbw.narratortest.NarratorTestClient;
import bbw.narratortest.EventCalls;

@Mixin(MinecraftServer.class)
public class ExampleMixin {
	@Inject(at = @At("HEAD"), method = "loadWorld")
	private void init(CallbackInfo info) {
		// This code is injected into the start of MinecraftServer.loadWorld()V

        NarratorTest.startTime = System.currentTimeMillis();
		NarratorTest.eventLogger.clear();

		EventCalls.clear();
		
	}
}