package com.ab1f5a.drudge.util;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.mixinterface.IMinecraftClient;

public final class OverlayRenderer
{
	protected static final Minecraft MC = DrudgeClient.MC;
	protected static final IMinecraftClient IMC = DrudgeClient.IMC;
	
	private float progress;
	private float prevProgress;
	private BlockPos prevPos;
	
	public void resetProgress()
	{
		progress = 0;
		prevProgress = 0;
		prevPos = null;
	}
	
	public void updateProgress()
	{
		prevProgress = progress;
		progress = MC.gameMode.destroyProgress;
		
		if(progress < prevProgress)
			prevProgress = progress;
	}
	
	public void render(PoseStack matrixStack, float partialTicks, BlockPos pos)
	{
		if(pos == null)
			return;
		
		if(prevPos != null && !pos.equals(prevPos))
			resetProgress();
		
		prevPos = pos;
		
		boolean breaksInstantly = MC.player.getAbilities().instabuild
			|| BlockUtils.getHardness(pos) >= 1;
		float p = breaksInstantly ? 1
			: Mth.lerp(partialTicks, prevProgress, progress);

		AABB outline = new AABB(pos).deflate(1 / 16.0);
		AABB fill = new AABB(pos).deflate(1 / 32.0).setMaxY(pos.getY() + p);

		RenderUtils.drawSolidBox(matrixStack, fill, 0x8000FF00, false);
		RenderUtils.drawOutlinedBox(matrixStack, outline, 0xFFFFFFFF, false);
	}
}
