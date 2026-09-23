package com.ab1f5a.drudge.mixin;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.events.RenderListener.RenderEvent;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin
{
	/**
	 * This version hands the level render its camera as a render state rather
	 * than as a matrix, so the position matrix comes off that state and the
	 * partial tick off the camera it was captured from.
	 */
	@Inject(
		method = "render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V",
		at = @At("RETURN"))
	private void onRender(GraphicsResourceAllocator resourceAllocator,
		boolean renderOutline, CameraRenderState cameraState,
		GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky,
		boolean consistentDepthRequired, CallbackInfo ci)
	{
		PoseStack matrixStack = new PoseStack();
		matrixStack.mulPose(cameraState.viewRotationMatrix);
		RenderEvent event =
			new RenderEvent(matrixStack, cameraState.cameraEntityPartialTicks);
		EventManager.fire(event);
	}
}
