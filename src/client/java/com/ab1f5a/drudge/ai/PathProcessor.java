package com.ab1f5a.drudge.ai;

import java.util.ArrayList;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.mixinterface.IKeyMapping;

public abstract class PathProcessor
{
	protected static final DrudgeClient DRUDGE = DrudgeClient.INSTANCE;
	protected static final Minecraft MC = DrudgeClient.MC;
	
	private static final KeyMapping[] CONTROLS =
		{MC.options.keyUp, MC.options.keyDown, MC.options.keyRight,
			MC.options.keyLeft, MC.options.keyJump, MC.options.keyShift};
	
	protected final ArrayList<PathPos> path;
	protected int index;
	protected boolean done;
	protected int ticksOffPath;
	
	public PathProcessor(ArrayList<PathPos> path)
	{
		if(path.isEmpty())
			throw new IllegalStateException("There is no path!");
		
		this.path = path;
	}
	
	public abstract void process();
	
	public abstract boolean canBreakBlocks();
	
	public final int getIndex()
	{
		return index;
	}
	
	public final boolean isDone()
	{
		return done;
	}
	
	public final int getTicksOffPath()
	{
		return ticksOffPath;
	}
	
	protected final void facePosition(BlockPos pos)
	{
		DRUDGE.getRotationFaker()
			.faceVectorClientIgnorePitch(Vec3.atCenterOf(pos));
	}
	
	public static final void lockControls()
	{
		for(KeyMapping key : CONTROLS)
			key.setDown(false);
		
		MC.player.setSprinting(false);
	}
	
	public static final void releaseControls()
	{
		for(KeyMapping key : CONTROLS)
			IKeyMapping.get(key).resetPressedState();
	}
}
