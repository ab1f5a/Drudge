package com.ab1f5a.drudge;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * This version dropped the {@code OutputTarget} concept: which framebuffer a
 * layer lands in is decided by the render pass it is drawn into rather than by
 * the layer itself, so the drudge layers no longer name one and go to the main
 * target like everything else.
 */
public enum DrudgeRenderLayers
{
	;

	/** Applied to the model-view matrix by {@link com.ab1f5a.drudge.util.EasyVertexBuffer}. */
	public static final LayeringTransform LAYERING =
		LayeringTransform.VIEW_OFFSET_Z_LAYERING;

	public static final RenderType LINES = RenderType.create("drudge:lines",
		RenderSetup.builder(DrudgeShaderPipelines.DEPTH_TEST_LINES)
			.setLayeringTransform(LAYERING)
			.createRenderSetup());

	public static final RenderType ESP_LINES =
		RenderType.create("drudge:esp_lines",
			RenderSetup.builder(DrudgeShaderPipelines.ESP_LINES)
				.setLayeringTransform(LAYERING)
				.createRenderSetup());
	
	public static final RenderType QUADS = RenderType.create("drudge:quads",
		RenderSetup.builder(DrudgeShaderPipelines.QUADS).sortOnUpload()
			.createRenderSetup());
	
	public static final RenderType ESP_QUADS = RenderType.create(
		"drudge:esp_quads", RenderSetup.builder(DrudgeShaderPipelines.ESP_QUADS)
			.sortOnUpload().createRenderSetup());
	
	public static final RenderType ESP_QUADS_NO_CULLING =
		RenderType.create("drudge:esp_quads_no_culling",
			RenderSetup.builder(DrudgeShaderPipelines.ESP_QUADS_NO_CULLING)
				.sortOnUpload().useLightmap().createRenderSetup());
	
	public static RenderType getQuads(boolean depthTest)
	{
		return depthTest ? QUADS : ESP_QUADS;
	}
	
	public static RenderType getLines(boolean depthTest)
	{
		return depthTest ? LINES : ESP_LINES;
	}
}
