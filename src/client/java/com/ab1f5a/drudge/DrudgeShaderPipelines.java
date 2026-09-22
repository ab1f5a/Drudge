package com.ab1f5a.drudge;

import java.util.Optional;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public enum DrudgeShaderPipelines
{
	;

	public static final Snippet FOGLESS_LINES_SNIPPET = RenderPipeline
		.builder(RenderPipelines.LINES_SNIPPET)
		.withVertexShader(Identifier.parse("drudge:core/fogless_lines"))
		.withFragmentShader(Identifier.parse("drudge:core/fogless_lines"))
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(false)
		.withVertexBinding(0,
			DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
		.withPrimitiveTopology(PrimitiveTopology.LINES).buildSnippet();

	public static final RenderPipeline DEPTH_TEST_LINES =
		RenderPipelines.register(RenderPipeline.builder(FOGLESS_LINES_SNIPPET)
			.withLocation(
				Identifier.parse("drudge:pipeline/drudge_depth_test_lines"))
			.withDepthStencilState(DepthStencilState.DEFAULT).build());

	public static final RenderPipeline ESP_LINES =
		RenderPipelines.register(RenderPipeline.builder(FOGLESS_LINES_SNIPPET)
			.withLocation(Identifier.parse("drudge:pipeline/drudge_esp_lines"))
			.withDepthStencilState(Optional.empty()).build());

	public static final RenderPipeline QUADS = RenderPipelines
		.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.parse("drudge:pipeline/drudge_quads"))
			.withDepthStencilState(DepthStencilState.DEFAULT).withCull(true)
			.build());

	public static final RenderPipeline ESP_QUADS = RenderPipelines
		.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.parse("drudge:pipeline/drudge_esp_quads"))
			.withDepthStencilState(Optional.empty()).withCull(true).build());

	public static final RenderPipeline ESP_QUADS_NO_CULLING = RenderPipelines
		.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(Identifier.parse("drudge:pipeline/drudge_esp_quads"))
			.withDepthStencilState(Optional.empty()).withCull(false).build());
}
