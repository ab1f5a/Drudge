package com.ab1f5a.drudge.features.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import com.ab1f5a.drudge.util.BlockUtils;

public enum TreeUtils
{
	;
	
	public static boolean isLog(BlockPos pos)
	{
		return BlockUtils.getState(pos).is(BlockTags.LOGS);
	}
	
	public static boolean isLeaves(BlockPos pos)
	{
		BlockState state = BlockUtils.getState(pos);
		return state.is(BlockTags.LEAVES) || state.is(BlockTags.WART_BLOCKS);
	}
}
