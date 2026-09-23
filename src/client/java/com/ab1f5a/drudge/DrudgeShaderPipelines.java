package com.ab1f5a.drudge;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

/**
 * On 1.21.x the pipeline builder still describes depth testing with
 * {@link DepthTestFunction} and blending with {@link BlendFunction} rather than
 * the {@code DepthStencilState}/{@code ColorTargetState} pair that 26.x uses.
 */
public enum DrudgeShaderPipelines
{
	;

	public static final Snippet FOGLESS_LINES_SNIPPET = RenderPipeline
		.builder(RenderPipelines.LINES_SNIPPET)
		.withVertexShader(ResourceLocation.parse("drudge:core/fogless_lines"))
		.withFragmentShader(ResourceLocation.parse("drudge:core/fogless_lines"))
		.withBlend(BlendFunction.TRANSLUCENT)
		.withCull(false)
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL,
			VertexFormat.Mode.LINES)
		.buildSnippet();

	public static final RenderPipeline DEPTH_TEST_LINES =
		RenderPipelines.register(RenderPipeline.builder(FOGLESS_LINES_SNIPPET)
			.withLocation(
				ResourceLocation.parse("drudge:pipeline/drudge_depth_test_lines"))
			.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
			.withDepthWrite(true).build());

	public static final RenderPipeline ESP_LINES =
		RenderPipelines.register(RenderPipeline.builder(FOGLESS_LINES_SNIPPET)
			.withLocation(ResourceLocation.parse("drudge:pipeline/drudge_esp_lines"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withDepthWrite(false).build());

	public static final RenderPipeline QUADS = RenderPipelines
		.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(ResourceLocation.parse("drudge:pipeline/drudge_quads"))
			.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
			.withDepthWrite(true).withCull(true).build());

	public static final RenderPipeline ESP_QUADS = RenderPipelines
		.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(ResourceLocation.parse("drudge:pipeline/drudge_esp_quads"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withDepthWrite(false).withCull(true).build());

	public static final RenderPipeline ESP_QUADS_NO_CULLING = RenderPipelines
		.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(ResourceLocation.parse("drudge:pipeline/drudge_esp_quads"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withDepthWrite(false).withCull(false).build());
}
