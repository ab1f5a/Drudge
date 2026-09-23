package com.ab1f5a.drudge.util;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.DrudgeRenderLayers;

public enum RenderUtils
{
	;
	
	public static void applyRegionalRenderOffset(PoseStack matrixStack)
	{
		applyRegionalRenderOffset(matrixStack, getCameraRegion());
	}
	
	public static void applyRegionalRenderOffset(PoseStack matrixStack,
		ChunkAccess chunk)
	{
		applyRegionalRenderOffset(matrixStack, RegionPos.of(chunk.getPos()));
	}
	
	public static void applyRegionalRenderOffset(PoseStack matrixStack,
		RegionPos region)
	{
		Vec3 offset = region.toVec3d().subtract(getCameraPos());
		matrixStack.translate(offset.x, offset.y, offset.z);
	}
	
	public static void applyRenderOffset(PoseStack matrixStack)
	{
		Vec3 camPos = getCameraPos();
		matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
	}
	
	public static Vec3 getCameraPos()
	{
		Camera camera = DrudgeClient.MC.gameRenderer.getMainCamera();
		if(camera == null)
			return Vec3.ZERO;
		
		return camera.position();
	}
	
	public static Rotation getCameraRotation()
	{
		Camera camera = DrudgeClient.MC.gameRenderer.getMainCamera();
		if(camera == null)
			return new Rotation(0, 0);
		
		return new Rotation(camera.getYRot(), camera.getXRot());
	}
	
	public static BlockPos getCameraBlockPos()
	{
		Camera camera = DrudgeClient.MC.gameRenderer.getMainCamera();
		if(camera == null)
			return BlockPos.ZERO;
		
		return camera.getBlockPosition();
	}
	
	public static RegionPos getCameraRegion()
	{
		return RegionPos.of(getCameraBlockPos());
	}
	
	public static float[] getRainbowColor()
	{
		float x = System.currentTimeMillis() % 2000 / 1000F;
		float pi = (float)Math.PI;
		
		float[] rainbow = new float[3];
		rainbow[0] = 0.5F + 0.5F * Mth.sin(x * pi);
		rainbow[1] = 0.5F + 0.5F * Mth.sin((x + 4F / 3F) * pi);
		rainbow[2] = 0.5F + 0.5F * Mth.sin((x + 8F / 3F) * pi);
		return rainbow;
	}
	
	public static int toIntColor(float[] rgb, float opacity)
	{
		return (int)(Mth.clamp(opacity, 0, 1) * 255) << 24
			| (int)(Mth.clamp(rgb[0], 0, 1) * 255) << 16
			| (int)(Mth.clamp(rgb[1], 0, 1) * 255) << 8
			| (int)(Mth.clamp(rgb[2], 0, 1) * 255);
	}
	
	public static void drawLine(PoseStack matrices, Vec3 start, Vec3 end,
		int color, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 offset = getCameraPos().reverse();
		drawLine(matrices, buffer, start.add(offset), end.add(offset), color);
		
		bs.uploadAndDraw();
	}
	
	private static Vec3 getTracerOrigin(float partialTicks)
	{
		return getCameraRotation().toLookVec().scale(10);
	}
	
	public static void drawTracer(PoseStack matrices, float partialTicks,
		Vec3 end, int color, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 start = getTracerOrigin(partialTicks);
		Vec3 offset = getCameraPos().reverse();
		drawLine(matrices, buffer, start, end.add(offset), color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawTracers(PoseStack matrices, float partialTicks,
		List<Vec3> ends, int color, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 start = getTracerOrigin(partialTicks);
		Vec3 offset = getCameraPos().reverse();
		for(Vec3 end : ends)
			drawLine(matrices, buffer, start, end.add(offset), color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawTracers(PoseStack matrices, float partialTicks,
		List<ColoredPoint> ends, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 start = getTracerOrigin(partialTicks);
		Vec3 offset = getCameraPos().reverse();
		for(ColoredPoint end : ends)
			drawLine(matrices, buffer, start, end.point().add(offset),
				end.color());
		
		bs.uploadAndDraw();
	}
	
	public static void drawLine(PoseStack matrices, VertexConsumer buffer,
		Vec3 start, Vec3 end, int color)
	{
		Pose entry = matrices.last();
		float x1 = (float)start.x;
		float y1 = (float)start.y;
		float z1 = (float)start.z;
		float x2 = (float)end.x;
		float y2 = (float)end.y;
		float z2 = (float)end.z;
		drawLine(entry, buffer, x1, y1, z1, x2, y2, z2, color);
	}
	
	public static void drawLine(PoseStack.Pose entry, VertexConsumer buffer,
		float x1, float y1, float z1, float x2, float y2, float z2, int color)
	{
		Vector3f normal = new Vector3f(x2, y2, z2).sub(x1, y1, z1).normalize();
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, normal) ;
		
		float t = new Vector3f(x1, y1, z1).negate().dot(normal);
		float length = new Vector3f(x2, y2, z2).sub(x1, y1, z1).length();
		if(t > 0 && t < length)
		{
			Vector3f closeToCam = new Vector3f(normal).mul(t).add(x1, y1, z1);
			buffer.addVertex(entry, closeToCam).setColor(color)
				.setNormal(entry, normal) ;
			buffer.addVertex(entry, closeToCam).setColor(color)
				.setNormal(entry, normal) ;
		}
		
		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, normal) ;
	}
	
	public static void drawLine(VertexConsumer buffer, float x1, float y1,
		float z1, float x2, float y2, float z2, int color)
	{
		Vector3f n = new Vector3f(x2, y2, z2).sub(x1, y1, z1).normalize();
		buffer.addVertex(x1, y1, z1).setColor(color).setNormal(n.x, n.y, n.z)
			 ;
		buffer.addVertex(x2, y2, z2).setColor(color).setNormal(n.x, n.y, n.z)
			 ;
	}
	
	public static void drawCurvedLine(PoseStack matrices, List<Vec3> points,
		int color, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 offset = getCameraPos().reverse();
		List<Vec3> points2 = points.stream().map(v -> v.add(offset)).toList();
		drawCurvedLine(matrices, buffer, points2, color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawCurvedLine(PoseStack matrices, VertexConsumer buffer,
		List<Vec3> points, int color)
	{
		if(points.size() < 2)
			return;
		
		PoseStack.Pose entry = matrices.last();
		
		for(int i = 1; i < points.size(); i++)
		{
			Vector3f prev = points.get(i - 1).toVector3f();
			Vector3f current = points.get(i).toVector3f();
			Vector3f normal = new Vector3f(current).sub(prev).normalize();
			buffer.addVertex(entry, prev).setColor(color)
				.setNormal(entry, normal) ;
			buffer.addVertex(entry, current).setColor(color)
				.setNormal(entry, normal) ;
		}
	}
	
	public static void drawSolidBox(PoseStack matrices, AABB box, int color,
		boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getQuads(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		drawSolidBox(matrices, buffer, box.move(getCameraPos().reverse()),
			color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawSolidBoxes(PoseStack matrices, List<AABB> boxes,
		int color, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getQuads(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 camOffset = getCameraPos().reverse();
		for(AABB box : boxes)
			drawSolidBox(matrices, buffer, box.move(camOffset), color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawSolidBoxes(PoseStack matrices,
		List<ColoredBox> boxes, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getQuads(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 camOffset = getCameraPos().reverse();
		for(ColoredBox box : boxes)
			drawSolidBox(matrices, buffer, box.box().move(camOffset),
				box.color());
		
		bs.uploadAndDraw();
	}
	
	public static void drawSolidBox(VertexConsumer buffer, AABB box, int color)
	{
		drawSolidBox(new PoseStack(), buffer, box, color);
	}
	
	public static void drawSolidBox(PoseStack matrices, VertexConsumer buffer,
		AABB box, int color)
	{
		PoseStack.Pose entry = matrices.last();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;
		
		buffer.addVertex(entry, x1, y1, z1).setColor(color);
		buffer.addVertex(entry, x2, y1, z1).setColor(color);
		buffer.addVertex(entry, x2, y1, z2).setColor(color);
		buffer.addVertex(entry, x1, y1, z2).setColor(color);
		
		buffer.addVertex(entry, x1, y2, z1).setColor(color);
		buffer.addVertex(entry, x1, y2, z2).setColor(color);
		buffer.addVertex(entry, x2, y2, z2).setColor(color);
		buffer.addVertex(entry, x2, y2, z1).setColor(color);
		
		buffer.addVertex(entry, x1, y1, z1).setColor(color);
		buffer.addVertex(entry, x1, y2, z1).setColor(color);
		buffer.addVertex(entry, x2, y2, z1).setColor(color);
		buffer.addVertex(entry, x2, y1, z1).setColor(color);
		
		buffer.addVertex(entry, x2, y1, z1).setColor(color);
		buffer.addVertex(entry, x2, y2, z1).setColor(color);
		buffer.addVertex(entry, x2, y2, z2).setColor(color);
		buffer.addVertex(entry, x2, y1, z2).setColor(color);
		
		buffer.addVertex(entry, x1, y1, z2).setColor(color);
		buffer.addVertex(entry, x2, y1, z2).setColor(color);
		buffer.addVertex(entry, x2, y2, z2).setColor(color);
		buffer.addVertex(entry, x1, y2, z2).setColor(color);
		
		buffer.addVertex(entry, x1, y1, z1).setColor(color);
		buffer.addVertex(entry, x1, y1, z2).setColor(color);
		buffer.addVertex(entry, x1, y2, z2).setColor(color);
		buffer.addVertex(entry, x1, y2, z1).setColor(color);
	}
	
	public static void drawOutlinedBox(PoseStack matrices, AABB box, int color,
		boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		drawOutlinedBox(matrices, buffer, box.move(getCameraPos().reverse()),
			color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawOutlinedBoxes(PoseStack matrices, List<AABB> boxes,
		int color, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 camOffset = getCameraPos().reverse();
		for(AABB box : boxes)
			drawOutlinedBox(matrices, buffer, box.move(camOffset), color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawOutlinedBoxes(PoseStack matrices,
		List<ColoredBox> boxes, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 camOffset = getCameraPos().reverse();
		for(ColoredBox box : boxes)
			drawOutlinedBox(matrices, buffer, box.box().move(camOffset),
				box.color());
		
		bs.uploadAndDraw();
	}
	
	public static void drawOutlinedBox(VertexConsumer buffer, AABB box,
		int color)
	{
		drawOutlinedBox(new PoseStack(), buffer, box, color);
	}
	
	public static void drawOutlinedBox(PoseStack matrices,
		VertexConsumer buffer, AABB box, int color)
	{
		PoseStack.Pose entry = matrices.last();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;
		
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, 1, 0, 0) ;
		buffer.addVertex(entry, x2, y1, z1).setColor(color)
			.setNormal(entry, 1, 0, 0) ;
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, 0, 0, 1) ;
		buffer.addVertex(entry, x1, y1, z2).setColor(color)
			.setNormal(entry, 0, 0, 1) ;
		buffer.addVertex(entry, x2, y1, z1).setColor(color)
			.setNormal(entry, 0, 0, 1) ;
		buffer.addVertex(entry, x2, y1, z2).setColor(color)
			.setNormal(entry, 0, 0, 1) ;
		buffer.addVertex(entry, x1, y1, z2).setColor(color)
			.setNormal(entry, 1, 0, 0) ;
		buffer.addVertex(entry, x2, y1, z2).setColor(color)
			.setNormal(entry, 1, 0, 0) ;
		
		buffer.addVertex(entry, x1, y2, z1).setColor(color)
			.setNormal(entry, 1, 0, 0) ;
		buffer.addVertex(entry, x2, y2, z1).setColor(color)
			.setNormal(entry, 1, 0, 0) ;
		buffer.addVertex(entry, x1, y2, z1).setColor(color)
			.setNormal(entry, 0, 0, 1) ;
		buffer.addVertex(entry, x1, y2, z2).setColor(color)
			.setNormal(entry, 0, 0, 1) ;
		buffer.addVertex(entry, x2, y2, z1).setColor(color)
			.setNormal(entry, 0, 0, 1) ;
		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, 0, 0, 1) ;
		buffer.addVertex(entry, x1, y2, z2).setColor(color)
			.setNormal(entry, 1, 0, 0) ;
		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, 1, 0, 0) ;
		
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, 0, 1, 0) ;
		buffer.addVertex(entry, x1, y2, z1).setColor(color)
			.setNormal(entry, 0, 1, 0) ;
		buffer.addVertex(entry, x2, y1, z1).setColor(color)
			.setNormal(entry, 0, 1, 0) ;
		buffer.addVertex(entry, x2, y2, z1).setColor(color)
			.setNormal(entry, 0, 1, 0) ;
		buffer.addVertex(entry, x1, y1, z2).setColor(color)
			.setNormal(entry, 0, 1, 0) ;
		buffer.addVertex(entry, x1, y2, z2).setColor(color)
			.setNormal(entry, 0, 1, 0) ;
		buffer.addVertex(entry, x2, y1, z2).setColor(color)
			.setNormal(entry, 0, 1, 0) ;
		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, 0, 1, 0) ;
	}
	
	public static void drawCrossBox(PoseStack matrices, AABB box, int color,
		boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		drawCrossBox(matrices, buffer, box.move(getCameraPos().reverse()),
			color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawCrossBoxes(PoseStack matrices, List<AABB> boxes,
		int color, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 camOffset = getCameraPos().reverse();
		for(AABB box : boxes)
			drawCrossBox(matrices, buffer, box.move(camOffset), color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawCrossBoxes(PoseStack matrices,
		List<ColoredBox> boxes, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 camOffset = getCameraPos().reverse();
		for(ColoredBox box : boxes)
			drawCrossBox(matrices, buffer, box.box().move(camOffset),
				box.color());
		
		bs.uploadAndDraw();
	}
	
	public static void drawCrossBox(VertexConsumer buffer, AABB box, int color)
	{
		drawCrossBox(new PoseStack(), buffer, box, color);
	}
	
	public static void drawCrossBox(PoseStack matrices, VertexConsumer buffer,
		AABB box, int color)
	{
		PoseStack.Pose entry = matrices.last();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;
		
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, 1, 1, 0) ;
		buffer.addVertex(entry, x2, y2, z1).setColor(color)
			.setNormal(entry, 1, 1, 0) ;
		buffer.addVertex(entry, x2, y1, z1).setColor(color)
			.setNormal(entry, -1, 1, 0) ;
		buffer.addVertex(entry, x1, y2, z1).setColor(color)
			.setNormal(entry, -1, 1, 0) ;
		
		buffer.addVertex(entry, x2, y1, z1).setColor(color)
			.setNormal(entry, 0, 1, 1) ;
		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, 0, 1, 1) ;
		buffer.addVertex(entry, x2, y1, z2).setColor(color)
			.setNormal(entry, 0, 1, -1) ;
		buffer.addVertex(entry, x2, y2, z1).setColor(color)
			.setNormal(entry, 0, 1, -1) ;
		
		buffer.addVertex(entry, x2, y1, z2).setColor(color)
			.setNormal(entry, -1, 1, 0) ;
		buffer.addVertex(entry, x1, y2, z2).setColor(color)
			.setNormal(entry, -1, 1, 0) ;
		buffer.addVertex(entry, x1, y1, z2).setColor(color)
			.setNormal(entry, 1, 1, 0) ;
		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, 1, 1, 0) ;
		
		buffer.addVertex(entry, x1, y1, z2).setColor(color)
			.setNormal(entry, 0, 1, -1) ;
		buffer.addVertex(entry, x1, y2, z1).setColor(color)
			.setNormal(entry, 0, 1, -1) ;
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, 0, 1, 1) ;
		buffer.addVertex(entry, x1, y2, z2).setColor(color)
			.setNormal(entry, 0, 1, 1) ;
		
		buffer.addVertex(entry, x1, y2, z2).setColor(color)
			.setNormal(entry, 1, 0, -1) ;
		buffer.addVertex(entry, x2, y2, z1).setColor(color)
			.setNormal(entry, 1, 0, -1) ;
		buffer.addVertex(entry, x1, y2, z1).setColor(color)
			.setNormal(entry, 1, 0, 1) ;
		buffer.addVertex(entry, x2, y2, z2).setColor(color)
			.setNormal(entry, 1, 0, 1) ;
		
		buffer.addVertex(entry, x2, y1, z1).setColor(color)
			.setNormal(entry, -1, 0, 1) ;
		buffer.addVertex(entry, x1, y1, z2).setColor(color)
			.setNormal(entry, -1, 0, 1) ;
		buffer.addVertex(entry, x1, y1, z1).setColor(color)
			.setNormal(entry, 1, 0, 1) ;
		buffer.addVertex(entry, x2, y1, z2).setColor(color)
			.setNormal(entry, 1, 0, 1) ;
	}
	
	public static void drawNode(PoseStack matrices, AABB box, int color,
		boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		drawNode(matrices, buffer, box.move(getCameraPos().reverse()), color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawNodes(PoseStack matrices, List<AABB> boxes,
		int color, boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 camOffset = getCameraPos().reverse();
		for(AABB box : boxes)
			drawNode(matrices, buffer, box.move(camOffset), color);
		
		bs.uploadAndDraw();
	}
	
	public static void drawNodes(PoseStack matrices, List<ColoredBox> boxes,
		boolean depthTest)
	{
		DrudgeBufferSource bs = new DrudgeBufferSource();
		RenderType layer = DrudgeRenderLayers.getLines(depthTest);
		VertexConsumer buffer = bs.getBuffer(layer);
		
		Vec3 camOffset = getCameraPos().reverse();
		for(ColoredBox box : boxes)
			drawNode(matrices, buffer, box.box().move(camOffset), box.color());
		
		bs.uploadAndDraw();
	}
	
	public static void drawNode(VertexConsumer buffer, AABB box, int color)
	{
		drawNode(new PoseStack(), buffer, box, color);
	}
	
	public static void drawNode(PoseStack matrices, VertexConsumer buffer,
		AABB box, int color)
	{
		PoseStack.Pose entry = matrices.last();
		float x1 = (float)box.minX;
		float y1 = (float)box.minY;
		float z1 = (float)box.minZ;
		float x2 = (float)box.maxX;
		float y2 = (float)box.maxY;
		float z2 = (float)box.maxZ;
		float x3 = (x1 + x2) / 2F;
		float y3 = (y1 + y2) / 2F;
		float z3 = (z1 + z2) / 2F;
		
		drawLine(entry, buffer, x3, y3, z2, x1, y3, z3, color);
		drawLine(entry, buffer, x1, y3, z3, x3, y3, z1, color);
		drawLine(entry, buffer, x3, y3, z1, x2, y3, z3, color);
		drawLine(entry, buffer, x2, y3, z3, x3, y3, z2, color);
		
		drawLine(entry, buffer, x3, y2, z3, x2, y3, z3, color);
		drawLine(entry, buffer, x3, y2, z3, x1, y3, z3, color);
		drawLine(entry, buffer, x3, y2, z3, x3, y3, z1, color);
		drawLine(entry, buffer, x3, y2, z3, x3, y3, z2, color);
		
		drawLine(entry, buffer, x3, y1, z3, x2, y3, z3, color);
		drawLine(entry, buffer, x3, y1, z3, x1, y3, z3, color);
		drawLine(entry, buffer, x3, y1, z3, x3, y3, z1, color);
		drawLine(entry, buffer, x3, y1, z3, x3, y3, z2, color);
	}
	
	public static void drawArrow(VertexConsumer buffer, Vec3 from, Vec3 to,
		int color, float headSize)
	{
		drawArrow(new PoseStack(), buffer, from, to, color, headSize);
	}
	
	public static void drawArrow(PoseStack matrices, VertexConsumer buffer,
		Vec3 from, Vec3 to, int color, float headSize)
	{
		matrices.pushPose();
		PoseStack.Pose entry = matrices.last();
		Matrix4f matrix = entry.pose();
		
		drawLine(matrices, buffer, from, to, color);
		
		matrices.translate(to);
		matrices.scale(headSize, headSize, headSize);
		
		double xDiff = to.x - from.x;
		double yDiff = to.y - from.y;
		double zDiff = to.z - from.z;
		
		float xAngle = (float)(Math.atan2(yDiff, -zDiff) + Math.toRadians(90));
		matrix.rotate(xAngle, new Vector3f(1, 0, 0));
		
		double yzDiff = Math.sqrt(yDiff * yDiff + zDiff * zDiff);
		float zAngle = (float)Math.atan2(xDiff, yzDiff);
		matrix.rotate(zAngle, new Vector3f(0, 0, 1));
		
		drawLine(entry, buffer, 0, 2, 1, -1, 2, 0, color);
		drawLine(entry, buffer, -1, 2, 0, 0, 2, -1, color);
		drawLine(entry, buffer, 0, 2, -1, 1, 2, 0, color);
		drawLine(entry, buffer, 1, 2, 0, 0, 2, 1, color);
		drawLine(entry, buffer, 1, 2, 0, -1, 2, 0, color);
		drawLine(entry, buffer, 0, 2, 1, 0, 2, -1, color);
		drawLine(entry, buffer, 0, 0, 0, 1, 2, 0, color);
		drawLine(entry, buffer, 0, 0, 0, -1, 2, 0, color);
		drawLine(entry, buffer, 0, 0, 0, 0, 2, -1, color);
		drawLine(entry, buffer, 0, 0, 0, 0, 2, 1, color);
		
		matrices.popPose();
	}
	
	public record ColoredPoint(Vec3 point, int color)
	{}
	
	public record ColoredBox(AABB box, int color)
	{}
}
