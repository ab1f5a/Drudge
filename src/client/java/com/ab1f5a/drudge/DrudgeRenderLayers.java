package com.ab1f5a.drudge;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * On this version {@link RenderType} is still built from the classic
 * {@link RenderStateShard} state descriptors rather than 26.x's
 * {@code RenderSetup}/{@code LayeringTransform}/{@code OutputTarget} trio.
 * The shader pipeline itself is still a {@code RenderPipeline}, so only the
 * state wrapper differs.
 */
public enum DrudgeRenderLayers
{
	;

	public static final RenderType LINES = RenderType.create("drudge:lines",
		RenderType.BIG_BUFFER_SIZE, false, false,
		DrudgeShaderPipelines.DEPTH_TEST_LINES,
		RenderType.CompositeState.builder()
			.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
			.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
			.createCompositeState(false));

	public static final RenderType ESP_LINES = RenderType.create("drudge:esp_lines",
		RenderType.BIG_BUFFER_SIZE, false, false,
		DrudgeShaderPipelines.ESP_LINES,
		RenderType.CompositeState.builder()
			.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
			.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
			.createCompositeState(false));

	public static final RenderType QUADS = RenderType.create("drudge:quads",
		RenderType.BIG_BUFFER_SIZE, false, true,
		DrudgeShaderPipelines.QUADS,
		RenderType.CompositeState.builder().createCompositeState(false));

	public static final RenderType ESP_QUADS = RenderType.create("drudge:esp_quads",
		RenderType.BIG_BUFFER_SIZE, false, true,
		DrudgeShaderPipelines.ESP_QUADS,
		RenderType.CompositeState.builder().createCompositeState(false));

	public static final RenderType ESP_QUADS_NO_CULLING =
		RenderType.create("drudge:esp_quads_no_culling",
			RenderType.BIG_BUFFER_SIZE, false, true,
			DrudgeShaderPipelines.ESP_QUADS_NO_CULLING,
			RenderType.CompositeState.builder()
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.createCompositeState(false));

	public static RenderType getQuads(boolean depthTest)
	{
		return depthTest ? QUADS : ESP_QUADS;
	}

	public static RenderType getLines(boolean depthTest)
	{
		return depthTest ? LINES : ESP_LINES;
	}
}
