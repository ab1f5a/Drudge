package com.ab1f5a.drudge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.LocalPlayer;
import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.events.PostMotionListener.PostMotionEvent;
import com.ab1f5a.drudge.events.PreMotionListener.PreMotionEvent;
import com.ab1f5a.drudge.events.UpdateListener.UpdateEvent;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin
{

	@Inject(method = "tick()V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V",
			ordinal = 0))
	private void onTick(CallbackInfo ci)
	{
		EventManager.fire(UpdateEvent.INSTANCE);
	}

	@Inject(method = "sendPosition()V", at = @At("HEAD"))
	private void onSendMovementPacketsHEAD(CallbackInfo ci)
	{
		EventManager.fire(PreMotionEvent.INSTANCE);
	}

	@Inject(method = "sendPosition()V", at = @At("TAIL"))
	private void onSendMovementPacketsTAIL(CallbackInfo ci)
	{
		EventManager.fire(PostMotionEvent.INSTANCE);
	}
}
