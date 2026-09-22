package com.ab1f5a.drudge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.events.GUIRenderListener.GUIRenderEvent;

@Mixin(Hud.class)
public class HudMixin
{

	@Inject(
		method = "extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
		at = @At("HEAD"))
	private void onRenderPlayerList(GuiGraphicsExtractor context,
		DeltaTracker tickCounter, CallbackInfo ci)
	{
		if(DrudgeClient.MC.debugEntries.isOverlayVisible())
			return;

		float tickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
		EventManager.fire(new GUIRenderEvent(context, tickDelta));
	}
}
