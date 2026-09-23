package com.ab1f5a.drudge.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import com.ab1f5a.drudge.util.DrudgeBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.DrudgeRenderLayers;
import com.ab1f5a.drudge.util.BlockUtils;
import com.ab1f5a.drudge.util.RegionPos;
import com.ab1f5a.drudge.util.RenderUtils;

public class PathFinder
{
	private static final Minecraft MC = DrudgeClient.MC;
	
	private final PlayerAbilities abilities = PlayerAbilities.get();
	protected boolean fallingAllowed = true;
	protected boolean divingAllowed = true;
	
	private final PathPos start;
	protected PathPos current;
	private final BlockPos goal;
	
	private final HashMap<PathPos, Float> costMap = new HashMap<>();
	protected final HashMap<PathPos, PathPos> prevPosMap = new HashMap<>();
	private final PathQueue queue = new PathQueue();
	
	protected int thinkSpeed = 1024;
	protected int thinkTime = 200;
	private int iterations;
	
	protected boolean done;
	protected boolean failed;
	private final ArrayList<PathPos> path = new ArrayList<>();
	
	public PathFinder(BlockPos goal)
	{
		if(MC.player.onGround())
			start = new PathPos(BlockPos.containing(MC.player.getX(),
				MC.player.getY() + 0.5, MC.player.getZ()));
		else
			start = new PathPos(BlockPos.containing(MC.player.position()));
		this.goal = goal;
		
		costMap.put(start, 0F);
		queue.add(start, getHeuristic(start));
	}
	
	public PathFinder(PathFinder pathFinder)
	{
		this(pathFinder.goal);
		thinkSpeed = pathFinder.thinkSpeed;
		thinkTime = pathFinder.thinkTime;
	}
	
	public void think()
	{
		if(done)
			throw new IllegalStateException("Path was already found!");
		
		int i = 0;
		for(; i < thinkSpeed && !checkFailed(); i++)
		{
			current = queue.poll();
			
			if(checkDone())
				return;
			
			for(PathPos next : getNeighbors(current))
			{
				float newCost = costMap.get(current) + getCost(current, next);
				if(costMap.containsKey(next) && costMap.get(next) <= newCost)
					continue;
				
				costMap.put(next, newCost);
				prevPosMap.put(next, current);
				queue.add(next, newCost + getHeuristic(next));
			}
		}
		iterations += i;
	}
	
	protected boolean checkDone()
	{
		return done = goal.equals(current);
	}
	
	private boolean checkFailed()
	{
		return failed = queue.isEmpty() || iterations >= thinkSpeed * thinkTime;
	}
	
	private ArrayList<PathPos> getNeighbors(PathPos pos)
	{
		ArrayList<PathPos> neighbors = new ArrayList<>();
		
		if(Math.abs(start.getX() - pos.getX()) > 256
			|| Math.abs(start.getZ() - pos.getZ()) > 256)
			return neighbors;
		
		BlockPos north = pos.north();
		BlockPos east = pos.east();
		BlockPos south = pos.south();
		BlockPos west = pos.west();
		
		BlockPos northEast = north.east();
		BlockPos southEast = south.east();
		BlockPos southWest = south.west();
		BlockPos northWest = north.west();
		
		BlockPos up = pos.above();
		BlockPos down = pos.below();
		
		boolean flying = canFlyAt(pos);
		boolean onGround = canBeSolid(down);
		
		if(flying || onGround || pos.isJumping()
			|| canMoveSidewaysInMidairAt(pos) || canClimbUpAt(pos.below()))
		{
			if(checkHorizontalMovement(pos, north))
				neighbors.add(new PathPos(north));
			
			if(checkHorizontalMovement(pos, east))
				neighbors.add(new PathPos(east));
			
			if(checkHorizontalMovement(pos, south))
				neighbors.add(new PathPos(south));
			
			if(checkHorizontalMovement(pos, west))
				neighbors.add(new PathPos(west));
			
			if(checkDiagonalMovement(pos, Direction.NORTH, Direction.EAST))
				neighbors.add(new PathPos(northEast));
			
			if(checkDiagonalMovement(pos, Direction.SOUTH, Direction.EAST))
				neighbors.add(new PathPos(southEast));
			
			if(checkDiagonalMovement(pos, Direction.SOUTH, Direction.WEST))
				neighbors.add(new PathPos(southWest));
			
			if(checkDiagonalMovement(pos, Direction.NORTH, Direction.WEST))
				neighbors.add(new PathPos(northWest));
		}
		
		if(pos.getY() < MC.level.getMaxY() && canGoThrough(up.above())
			&& (flying || onGround || canClimbUpAt(pos))
			&& (flying || canClimbUpAt(pos) || goal.equals(up)
				|| canSafelyStandOn(north) || canSafelyStandOn(east)
				|| canSafelyStandOn(south) || canSafelyStandOn(west))
			&& (divingAllowed
				|| BlockUtils.getBlock(up.above()) != Blocks.WATER))
			neighbors.add(new PathPos(up, onGround));
		
		if(pos.getY() > MC.level.getMinY() && canGoThrough(down)
			&& canGoAbove(down.below()) && (flying || canFallBelow(pos))
			&& (divingAllowed || BlockUtils.getBlock(pos) != Blocks.WATER))
			neighbors.add(new PathPos(down));
		
		return neighbors;
	}
	
	private boolean checkHorizontalMovement(BlockPos current, BlockPos next)
	{
		if(isPassable(next) && (canFlyAt(current) || canGoThrough(next.below())
			|| canSafelyStandOn(next.below())))
			return true;
		
		return false;
	}
	
	private boolean checkDiagonalMovement(BlockPos current,
		Direction direction1, Direction direction2)
	{
		BlockPos horizontal1 = current.relative(direction1);
		BlockPos horizontal2 = current.relative(direction2);
		BlockPos next = horizontal1.relative(direction2);
		
		if(isPassableWithoutMining(horizontal1)
			&& isPassableWithoutMining(horizontal2)
			&& checkHorizontalMovement(current, next))
			return true;
		
		return false;
	}
	
	protected boolean isPassable(BlockPos pos)
	{
		if(!canGoThrough(pos) && !isMineable(pos))
			return false;
		
		BlockPos up = pos.above();
		if(!canGoThrough(up) && !isMineable(up))
			return false;
		
		if(!canGoAbove(pos.below()))
			return false;
		
		if(!divingAllowed && BlockUtils.getBlock(up) == Blocks.WATER)
			return false;
		
		return true;
	}
	
	protected boolean isPassableWithoutMining(BlockPos pos)
	{
		if(!canGoThrough(pos))
			return false;
		
		BlockPos up = pos.above();
		if(!canGoThrough(up))
			return false;
		
		if(!canGoAbove(pos.below()))
			return false;
		
		if(!divingAllowed && BlockUtils.getBlock(up) == Blocks.WATER)
			return false;
		
		return true;
	}
	
	protected boolean isMineable(BlockPos pos)
	{
		return false;
	}
	
	@SuppressWarnings("deprecation")
	protected boolean canBeSolid(BlockPos pos)
	{
		BlockState state = BlockUtils.getState(pos);
		Block block = state.getBlock();
		
		return state.blocksMotion() && !(block instanceof SignBlock)
			|| block instanceof LadderBlock || abilities.jesus()
				&& (block == Blocks.WATER || block == Blocks.LAVA);
	}
	
	@SuppressWarnings("deprecation")
	private boolean canGoThrough(BlockPos pos)
	{
		if(!MC.level.hasChunkAt(pos))
			return false;
		
		BlockState state = BlockUtils.getState(pos);
		Block block = state.getBlock();
		if(state.blocksMotion() && !(block instanceof SignBlock))
			return false;
		
		if(block instanceof TripWireBlock
			|| block instanceof PressurePlateBlock)
			return false;
		
		if(!abilities.invulnerable()
			&& (block == Blocks.LAVA || block instanceof BaseFireBlock))
			return false;
		
		return true;
	}
	
	private boolean canGoAbove(BlockPos pos)
	{
		Block block = BlockUtils.getBlock(pos);
		if(block instanceof FenceBlock || block instanceof WallBlock
			|| block instanceof FenceGateBlock)
			return false;
		
		return true;
	}
	
	private boolean canSafelyStandOn(BlockPos pos)
	{
		if(!canBeSolid(pos))
			return false;
		
		BlockState state = BlockUtils.getState(pos);
		Fluid fluid = state.getFluidState().getType();
		if(!abilities.invulnerable() && (state.getBlock() instanceof CactusBlock
			|| fluid instanceof LavaFluid))
			return false;
		
		return true;
	}
	
	private boolean canFallBelow(PathPos pos)
	{
		BlockPos down2 = pos.below(2);
		if(fallingAllowed && canGoThrough(down2))
			return true;
		
		if(!canSafelyStandOn(down2))
			return false;
		
		if(abilities.immuneToFallDamage() && fallingAllowed)
			return true;
		
		if(BlockUtils.getBlock(down2) instanceof SlimeBlock && fallingAllowed)
			return true;
		
		BlockPos prevPos = pos;
		for(int i = 0; i <= (fallingAllowed ? 3 : 1); i++)
		{
			if(prevPos == null)
				return true;
				
			if(!pos.above(i).equals(prevPos))
				return true;
			
			Block prevBlock = BlockUtils.getBlock(prevPos);
			BlockState prevState = BlockUtils.getState(prevPos);
			if(prevState.getFluidState().getType() instanceof WaterFluid
				|| prevBlock instanceof LadderBlock
				|| prevBlock instanceof VineBlock
				|| prevBlock instanceof WebBlock)
				return true;
			
			prevPos = prevPosMap.get(prevPos);
		}
		
		return false;
	}
	
	private boolean canFlyAt(BlockPos pos)
	{
		return abilities.flying() || !abilities.noWaterSlowdown()
			&& BlockUtils.getBlock(pos) == Blocks.WATER;
	}
	
	private boolean canClimbUpAt(BlockPos pos)
	{
		Block block = BlockUtils.getBlock(pos);
		if(!abilities.spider() && !(block instanceof LadderBlock)
			&& !(block instanceof VineBlock))
			return false;
		
		BlockPos up = pos.above();
		if(!canBeSolid(pos.north()) && !canBeSolid(pos.east())
			&& !canBeSolid(pos.south()) && !canBeSolid(pos.west())
			&& !canBeSolid(up.north()) && !canBeSolid(up.east())
			&& !canBeSolid(up.south()) && !canBeSolid(up.west()))
			return false;
		
		return true;
	}
	
	private boolean canMoveSidewaysInMidairAt(BlockPos pos)
	{
		Block blockFeet = BlockUtils.getBlock(pos);
		if(BlockUtils.getBlock(pos) instanceof LiquidBlock
			|| blockFeet instanceof LadderBlock
			|| blockFeet instanceof VineBlock || blockFeet instanceof WebBlock)
			return true;
		
		Block blockHead = BlockUtils.getBlock(pos.above());
		if(BlockUtils.getBlock(pos.above()) instanceof LiquidBlock
			|| blockHead instanceof WebBlock)
			return true;
		
		return false;
	}
	
	private float getCost(BlockPos current, BlockPos next)
	{
		float[] costs = {0.5F, 0.5F};
		BlockPos[] positions = {current, next};
		
		for(int i = 0; i < positions.length; i++)
		{
			BlockPos pos = positions[i];
			Block block = BlockUtils.getBlock(pos);
			
			if(block == Blocks.WATER && !abilities.noWaterSlowdown())
				costs[i] *= 1.3164437838225804F;
			else if(block == Blocks.LAVA)
				costs[i] *= 4.539515393656079F;
			
			if(!canFlyAt(pos)
				&& BlockUtils.getBlock(pos.below()) instanceof SoulSandBlock)
				costs[i] *= 2.5F;
			
			if(isMineable(pos))
				costs[i] *= 2F;
			if(isMineable(pos.above()))
				costs[i] *= 2F;
		}
		
		float cost = costs[0] + costs[1];
		
		if(current.getX() != next.getX() && current.getZ() != next.getZ())
			cost *= 1.4142135623730951F;
		
		return cost;
	}
	
	private float getHeuristic(BlockPos pos)
	{
		float dx = Math.abs(pos.getX() - goal.getX());
		float dy = Math.abs(pos.getY() - goal.getY());
		float dz = Math.abs(pos.getZ() - goal.getZ());
		return 1.001F * (dx + dy + dz - 0.5857864376269049F * Math.min(dx, dz));
	}
	
	public PathPos getCurrentPos()
	{
		return current;
	}
	
	public BlockPos getGoal()
	{
		return goal;
	}
	
	public int countProcessedBlocks()
	{
		return prevPosMap.size();
	}
	
	public int getQueueSize()
	{
		return queue.size();
	}
	
	public float getCost(BlockPos pos)
	{
		return costMap.get(pos);
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	public boolean isFailed()
	{
		return failed;
	}
	
	public ArrayList<PathPos> formatPath()
	{
		if(!done && !failed)
			throw new IllegalStateException("No path found!");
		if(!path.isEmpty())
			throw new IllegalStateException("Path was already formatted!");
		
		PathPos pos;
		if(!failed)
			pos = current;
		else
		{
			pos = start;
			for(PathPos next : prevPosMap.keySet())
				if(getHeuristic(next) < getHeuristic(pos)
					&& (canFlyAt(next) || canBeSolid(next.below())))
					pos = next;
		}
		
		while(pos != null)
		{
			path.add(pos);
			pos = prevPosMap.get(pos);
		}
		
		Collections.reverse(path);
		
		return path;
	}
	
	public void renderPath(PoseStack matrixStack, boolean depthTest)
	{
		if(path.size() < 2)
			return;

		RegionPos region = RenderUtils.getCameraRegion();
		Vec3 regionOffset = region.negate().toVec3d();

		matrixStack.pushPose();
		RenderUtils.applyRegionalRenderOffset(matrixStack, region);

		DrudgeBufferSource bs = new DrudgeBufferSource();
		VertexConsumer buffer =
			bs.getBuffer(DrudgeRenderLayers.getLines(depthTest));

		for(int i = 0; i < path.size() - 1; i++)
			RenderUtils.drawLine(matrixStack, buffer,
				Vec3.atCenterOf(path.get(i)).add(regionOffset),
				Vec3.atCenterOf(path.get(i + 1)).add(regionOffset),
				0xFFFF0000);

		matrixStack.popPose();
		bs.uploadAndDraw();
	}
	
	public boolean isPathStillValid(int index)
	{
		if(path.isEmpty())
			throw new IllegalStateException("Path is not formatted!");
		
		if(!abilities.equals(PlayerAbilities.get()))
			return false;
		
		if(index == 0)
		{
			PathPos pos = path.get(0);
			if(!isPassable(pos) || !canFlyAt(pos) && !canGoThrough(pos.below())
				&& !canSafelyStandOn(pos.below()))
				return false;
		}
		
		for(int i = Math.max(1, index); i < path.size(); i++)
			if(!getNeighbors(path.get(i - 1)).contains(path.get(i)))
				return false;
			
		return true;
	}
	
	public PathProcessor getProcessor()
	{
		if(abilities.flying())
			return new FlyPathProcessor(path, abilities.creativeFlying());
		
		return new WalkPathProcessor(path);
	}
	
	public void setThinkSpeed(int thinkSpeed)
	{
		this.thinkSpeed = thinkSpeed;
	}
	
	public void setThinkTime(int thinkTime)
	{
		this.thinkTime = thinkTime;
	}
	
	public void setFallingAllowed(boolean fallingAllowed)
	{
		this.fallingAllowed = fallingAllowed;
	}
	
	public void setDivingAllowed(boolean divingAllowed)
	{
		this.divingAllowed = divingAllowed;
	}
	
	public List<PathPos> getPath()
	{
		return Collections.unmodifiableList(path);
	}
}
