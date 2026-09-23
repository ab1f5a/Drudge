package com.ab1f5a.drudge.util;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

/**
 * Holds one batch of vertices and draws it on demand.
 *
 * <p>The newer versions upload the batch into a GPU buffer once and re-issue the
 * draw call per frame. That GPU buffer API does not exist yet here, so the batch
 * is kept as CPU-side {@link MeshData} and re-uploaded by
 * {@link RenderType#draw(MeshData)} on each draw instead. The vertices
 * themselves are still built only once, at construction, so the contents of a
 * buffer stay a snapshot of the moment it was created.
 */
public final class EasyVertexBuffer implements AutoCloseable
{
	private final ByteBufferBuilder byteBufferBuilder;
	private final MeshData meshData;

	public static EasyVertexBuffer createAndUpload(VertexFormat.Mode drawMode,
		VertexFormat format, Consumer<VertexConsumer> callback)
	{
		return new EasyVertexBuffer(drawMode, format, callback);
	}

	private EasyVertexBuffer(VertexFormat.Mode drawMode, VertexFormat format,
		Consumer<VertexConsumer> callback)
	{
		byteBufferBuilder = new ByteBufferBuilder(256);

		BufferBuilder bufferBuilder =
			new BufferBuilder(byteBufferBuilder, drawMode, format);
		callback.accept(bufferBuilder);
		meshData = bufferBuilder.build();
	}

	public void draw(PoseStack matrixStack, RenderType layer)
	{
		draw(matrixStack, layer, 1, 1, 1, 1);
	}

	public void draw(PoseStack matrixStack, RenderType layer, int argb)
	{
		float alpha = (argb >> 24 & 0xFF) / 255F;
		float red = (argb >> 16 & 0xFF) / 255F;
		float green = (argb >> 8 & 0xFF) / 255F;
		float blue = (argb & 0xFF) / 255F;
		draw(matrixStack, layer, red, green, blue, alpha);
	}

	public void draw(PoseStack matrixStack, RenderType layer, float[] rgba)
	{
		draw(matrixStack, layer, rgba[0], rgba[1], rgba[2], rgba[3]);
	}

	public void draw(PoseStack matrixStack, RenderType layer, float[] rgb,
		float alpha)
	{
		draw(matrixStack, layer, rgb[0], rgb[1], rgb[2], alpha);
	}

	public void draw(PoseStack matrixStack, RenderType layer, float red,
		float green, float blue, float alpha)
	{
		if(meshData == null)
			return;

		matrixStack.pushPose();

		RenderSystem.setShaderColor(red, green, blue, alpha);

		// The layer's shaders talk in terms of the model-view matrix, not the
		// pose stack, so the pose has to be folded in before drawing.
		RenderSystem.getModelViewStack().pushMatrix();
		RenderSystem.getModelViewStack().mul(matrixStack.last().pose());
		RenderSystem.applyModelViewMatrix();

		// RenderType.draw re-uploads the batch from this buffer, which only
		// reads from the start of it once rewound.
		meshData.vertexBuffer().rewind();
		layer.draw(meshData);

		RenderSystem.getModelViewStack().popMatrix();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.setShaderColor(1, 1, 1, 1);

		matrixStack.popPose();
	}

	@Override
	public void close()
	{
		if(meshData != null)
			meshData.close();

		byteBufferBuilder.close();
	}
}
