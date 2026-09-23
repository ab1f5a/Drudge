package com.ab1f5a.drudge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.events.GUIRenderListener.GUIRenderEvent;

@Mixin(Gui.class)
public class HudMixin
{

	@Inject(
		method = "renderTabList(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
		at = @At("HEAD"))
	private void onRenderPlayerList(GuiGraphics context,
		DeltaTracker tickCounter, CallbackInfo ci)
	{
		if(DrudgeClient.MC.getDebugOverlay().showDebugScreen())
			return;

		float tickDelta = tickCounter.getGameTimeDeltaPartialTick(true);
		EventManager.fire(new GUIRenderEvent(context, tickDelta));
	}
}
