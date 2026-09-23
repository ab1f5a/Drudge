package com.ab1f5a.drudge.features;

import com.ab1f5a.drudge.events.UpdateListener;
import com.ab1f5a.drudge.Feature;

public final class BreakFlow extends Feature implements UpdateListener
{
	public BreakFlow()
	{
		super("BreakFlow");
	}

	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}

	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}

	@Override
	public void onUpdate()
	{
		MC.gameMode.destroyDelay = 0;
	}
}
