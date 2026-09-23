package com.ab1f5a.drudge.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.events.RenderListener.RenderEvent;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin
{

	/**
	 * The sixth parameter is the matrix GameRenderer builds from the inverse
	 * camera rotation — the same thing 26.x calls the position matrix — and the
	 * seventh is the projection matrix.
	 */
	@Inject(
		method = "renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
		at = @At("RETURN"))
	private void onRender(DeltaTracker tickCounter, boolean renderBlockOutline,
		Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
		Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci)
	{
		PoseStack matrixStack = new PoseStack();
		matrixStack.mulPose(positionMatrix);
		float tickProgress = tickCounter.getGameTimeDeltaPartialTick(false);
		RenderEvent event = new RenderEvent(matrixStack, tickProgress);
		EventManager.fire(event);
	}
}
