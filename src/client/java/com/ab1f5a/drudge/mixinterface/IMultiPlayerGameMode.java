package com.ab1f5a.drudge.mixinterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public interface IMultiPlayerGameMode
{
	public void windowClick_QUICK_MOVE(int slot);

	public void windowClick_SWAP(int from, int to);

	public void rightClickBlock(BlockPos pos, Direction side, Vec3 hitVec);
}
