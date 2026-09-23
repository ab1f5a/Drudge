package com.ab1f5a.drudge;

import java.util.OptionalDouble;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * This version predates the RenderPipeline system entirely: a {@link RenderType}
 * is assembled straight from the vanilla {@link RenderStateShard} descriptors and
 * draws with the vanilla shaders. That is why this version has no
 * {@code DrudgeShaderPipelines} counterpart — the custom wide-line shader the
 * newer versions use has nothing to plug into here, so lines fall back to
 * {@link RenderStateShard#RENDERTYPE_LINES_SHADER} at a fixed 2px width.
 */
public enum DrudgeRenderLayers
{
	;

	/** Matches the 1536 the vanilla line and debug-quad layers use. */
	private static final int BUFFER_SIZE = 1536;

	/** The width the newer versions carried per-vertex as {@code setLineWidth(2)}. */
	private static final double LINE_WIDTH = 2.0D;

	public static final RenderType LINES = lines("drudge:lines", true);
	public static final RenderType ESP_LINES = lines("drudge:esp_lines", false);

	public static final RenderType QUADS = quads("drudge:quads", true);
	public static final RenderType ESP_QUADS = quads("drudge:esp_quads", false);
	public static final RenderType ESP_QUADS_NO_CULLING =
		quads("drudge:esp_quads_no_culling", false);

	private static RenderType lines(String name, boolean depthTest)
	{
		RenderType.CompositeState state = RenderType.CompositeState.builder()
			.setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
			.setLineState(
				new RenderStateShard.LineStateShard(OptionalDouble.of(LINE_WIDTH)))
			.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(depthTest ? RenderStateShard.LEQUAL_DEPTH_TEST
				: RenderStateShard.NO_DEPTH_TEST)
			.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
			.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
			.setCullState(RenderStateShard.NO_CULL)
			.createCompositeState(false);

		return RenderType.create(name, DefaultVertexFormat.POSITION_COLOR_NORMAL,
			VertexFormat.Mode.LINES, BUFFER_SIZE, false, false, state);
	}

	/**
	 * The no-culling variant is the one the newer versions also lightmapped, so
	 * it is the only quad layer that asks for a lightmap here.
	 */
	private static RenderType quads(String name, boolean depthTest)
	{
		boolean lightmap = name.endsWith("no_culling");

		RenderType.CompositeState state = RenderType.CompositeState.builder()
			.setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(depthTest ? RenderStateShard.LEQUAL_DEPTH_TEST
				: RenderStateShard.NO_DEPTH_TEST)
			.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
			.setCullState(lightmap ? RenderStateShard.NO_CULL
				: RenderStateShard.CULL)
			.setLightmapState(lightmap ? RenderStateShard.LIGHTMAP
				: RenderStateShard.NO_LIGHTMAP)
			.createCompositeState(false);

		return RenderType.create(name, DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS, BUFFER_SIZE, false, true, state);
	}

	public static RenderType getQuads(boolean depthTest)
	{
		return depthTest ? QUADS : ESP_QUADS;
	}

	public static RenderType getLines(boolean depthTest)
	{
		return depthTest ? LINES : ESP_LINES;
	}
}
