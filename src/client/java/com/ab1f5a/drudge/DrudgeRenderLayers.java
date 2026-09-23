package com.ab1f5a.drudge;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public enum DrudgeRenderLayers
{
	;
	
	public static final RenderType LINES = RenderType.create("drudge:lines",
		RenderSetup.builder(DrudgeShaderPipelines.DEPTH_TEST_LINES)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup());
	
	public static final RenderType ESP_LINES =
		RenderType.create("drudge:esp_lines",
			RenderSetup.builder(DrudgeShaderPipelines.ESP_LINES)
				.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
				.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
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
