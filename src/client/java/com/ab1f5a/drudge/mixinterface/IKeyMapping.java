package com.ab1f5a.drudge.mixinterface;

import net.minecraft.client.KeyMapping;

public interface IKeyMapping
{

	public default void resetPressedState()
	{
		drudge_resetPressedState();
	}

	public static IKeyMapping get(KeyMapping kb)
	{
		return (IKeyMapping)kb;
	}

	@Deprecated
	public void drudge_resetPressedState();
}
