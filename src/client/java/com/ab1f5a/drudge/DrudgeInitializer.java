package com.ab1f5a.drudge;

import net.fabricmc.api.ClientModInitializer;

public final class DrudgeInitializer implements ClientModInitializer
{
	private static boolean initialized;

	@Override
	public void onInitializeClient()
	{
		if(initialized)
			throw new RuntimeException(
				"DrudgeInitializer.onInitializeClient() ran twice!");

		DrudgeClient.INSTANCE.initialize();
		initialized = true;
	}
}
