package com.ab1f5a.drudge.util;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.MeshData.DrawState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;

import com.ab1f5a.drudge.DrudgeRenderLayers;

public final class EasyVertexBuffer implements AutoCloseable
{
	private final RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer;
	private final GpuBuffer vertexBuffer;
	private final int indexCount;
	private final PrimitiveTopology topology;

	public static EasyVertexBuffer createAndUpload(PrimitiveTopology drawMode,
		VertexFormat format, Consumer<VertexConsumer> callback)
	{
		try(ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256))
		{
			BufferBuilder bufferBuilder =
				new BufferBuilder(byteBufferBuilder, drawMode, format);
			callback.accept(bufferBuilder);

			try(MeshData buffer = bufferBuilder.build())
			{
				if(buffer == null)
					return new EasyVertexBuffer(drawMode);

				return new EasyVertexBuffer(buffer, drawMode);
			}
		}
	}

	private EasyVertexBuffer(MeshData buffer, PrimitiveTopology drawMode)
	{
		DrawState drawParams = buffer.drawState();
		shapeIndexBuffer =
			RenderSystem.getSequentialBuffer(drawParams.primitiveTopology());
		indexCount = drawParams.indexCount();
		topology = drawParams.primitiveTopology();

		vertexBuffer = RenderSystem.getDevice().createBuffer(null,
			GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
			buffer.vertexBuffer());
	}

	private EasyVertexBuffer(PrimitiveTopology drawMode)
	{
		shapeIndexBuffer = null;
		indexCount = 0;
		topology = drawMode;
		vertexBuffer = null;
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
		if(vertexBuffer == null)
			return;

		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.mul(matrixStack.last().pose());

		GpuBufferSlice gpuBufferSlice = writeTransforms(red, green, blue, alpha);

		// RenderType.prepare() already resolves the pipeline, its textures and
		// the current scissor box; only the transform block differs, because
		// prepare() hardcodes a white modulator and drudge tints per draw.
		PreparedRenderType base = layer.prepare();
		PreparedRenderType prepared = new PreparedRenderType(base.name(),
			base.pipeline(), base.oitPipelineSet(), gpuBufferSlice,
			base.scissorState(), base.textures());

		// The shared sequential index buffer is allocated on demand, and
		// ExecuteInfo.indexBuffer() reads it back through the no-arg getter,
		// which only returns the field. Passing null would therefore hand the
		// render pass an unallocated buffer; getBuffer(int) is the overload
		// that allocates. It can also widen the index type as it grows, so the
		// type has to be read after the buffer, not before.
		GpuBuffer sequentialIndices = shapeIndexBuffer.getBuffer(indexCount);

		StagedVertexBuffer.ExecuteInfo info = new StagedVertexBuffer.ExecuteInfo(
			vertexBuffer, sequentialIndices, shapeIndexBuffer.type(), 0, 0,
			indexCount, topology);

		RenderTarget framebuffer =
			Minecraft.getInstance().gameRenderer.mainRenderTarget();

		try(RenderPass renderPass =
			RenderSystem.getDevice().createCommandEncoder().createRenderPass(
				() -> "Drudge EasyVertexBuffer",
				framebuffer.getColorTextureView(), Optional.empty(),
				framebuffer.getDepthTextureView(), OptionalDouble.empty()))
		{
			prepared.drawFromBuffer(info, renderPass);
		}

		modelViewStack.popMatrix();
	}

	/**
	 * Mirrors what {@code RenderType.prepare()} does to the transform block —
	 * including the layer's Z-offset so lines do not z-fight with the terrain —
	 * but keeps the colour modulator instead of overwriting it with white.
	 */
	private GpuBufferSlice writeTransforms(float red, float green, float blue,
		float alpha)
	{
		Matrix4f modelView = RenderSystem.getModelViewMatrixCopy();
		DrudgeRenderLayers.LAYERING.getModifier().accept(modelView);

		return RenderSystem.getDynamicUniforms().writeTransform(modelView,
			new Vector4f(red, green, blue, alpha), new Vector3f(),
			TextureTransform.DEFAULT_TEXTURING.createMatrix());
	}

	@Override
	public void close()
	{
		if(vertexBuffer != null)
			vertexBuffer.close();
	}
}
