package com.ab1f5a.drudge.util;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Collects vertices for one or more render types and flushes them in a single
 * batch.
 *
 * <p>On 26.1.x the staged vertex buffer that 26.2 builds on does not exist yet,
 * so this is built on {@link MultiBufferSource.BufferSource} instead. The public
 * shape is kept identical, which is why none of the drawing call sites had to
 * change.
 */
public final class DrudgeBufferSource
{
	private final ByteBufferBuilder sharedBuffer =
		new ByteBufferBuilder(RenderType.BIG_BUFFER_SIZE);
	private final MultiBufferSource.BufferSource bufferSource =
		MultiBufferSource.immediate(sharedBuffer);

	public VertexConsumer getBuffer(RenderType renderType)
	{
		return bufferSource.getBuffer(renderType);
	}

	public void uploadAndDraw()
	{
		try
		{
			bufferSource.endBatch();

		}finally
		{
			sharedBuffer.close();
		}
	}
}
