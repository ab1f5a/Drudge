package com.ab1f5a.drudge.ai;

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.util.BlockUtils;
import com.ab1f5a.drudge.util.RotationUtils;

public class WalkPathProcessor extends PathProcessor
{
	public WalkPathProcessor(ArrayList<PathPos> path)
	{
		super(path);
	}
	
	@Override
	public void process()
	{
		BlockPos pos;
		if(DrudgeClient.MC.player.onGround())
			pos = BlockPos.containing(DrudgeClient.MC.player.getX(),
				DrudgeClient.MC.player.getY() + 0.5,
				DrudgeClient.MC.player.getZ());
		else
			pos = BlockPos.containing(DrudgeClient.MC.player.position());
		PathPos nextPos = path.get(index);
		int posIndex = path.indexOf(pos);
		
		if(posIndex == -1)
			ticksOffPath++;
		else
			ticksOffPath = 0;
		
		if(pos.equals(nextPos))
		{
			index++;
			
			if(index >= path.size())
				done = true;
			return;
		}
		if(posIndex > index)
		{
			index = posIndex + 1;
			
			if(index >= path.size())
				done = true;
			return;
		}
		
		lockControls();
		DrudgeClient.MC.player.getAbilities().flying = false;
		
		facePosition(nextPos);
		if(Mth.wrapDegrees(Math.abs(RotationUtils
			.getHorizontalAngleToLookVec(Vec3.atCenterOf(nextPos)))) > 90)
			return;
		
		if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
		{
			MC.options.keyUp.setDown(true);
			
			if(index > 0 && path.get(index - 1).isJumping()
				|| pos.getY() < nextPos.getY())
				MC.options.keyJump.setDown(true);
			
		}else if(pos.getY() != nextPos.getY())
			if(pos.getY() < nextPos.getY())
			{
				Block block = BlockUtils.getBlock(pos);
				if(block instanceof LadderBlock || block instanceof VineBlock)
				{
					DRUDGE.getRotationFaker().faceVectorClientIgnorePitch(
						BlockUtils.getBoundingBox(pos).getCenter());
					
					MC.options.keyUp.setDown(true);
					
				}else
				{
					if(index < path.size() - 1
						&& !nextPos.above().equals(path.get(index + 1)))
						index++;
					
					MC.options.keyJump.setDown(true);
				}
				
			}else
			{
				while(index < path.size() - 1
					&& path.get(index).below().equals(path.get(index + 1)))
					index++;
				
				if(DrudgeClient.MC.player.onGround())
					MC.options.keyUp.setDown(true);
			}
	}
	
	@Override
	public boolean canBreakBlocks()
	{
		return MC.player.onGround();
	}
}
