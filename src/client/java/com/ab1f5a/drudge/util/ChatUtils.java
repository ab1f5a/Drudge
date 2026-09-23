package com.ab1f5a.drudge.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import com.ab1f5a.drudge.DrudgeClient;

public final class ChatUtils
{
	private static final Minecraft MC = DrudgeClient.MC;

	private static final String PREFIX_KEY = "chat.drudge.prefix.drudge";
	private static final String ERROR_PREFIX_KEY = "chat.drudge.prefix.error";

	private ChatUtils()
	{

	}

	public static void component(Component component)
	{
		MC.gui.getChat()
			.addMessage(Component.translatable(PREFIX_KEY)
				.append(component));
	}

	public static void error(Component message)
	{
		component(Component.translatable(ERROR_PREFIX_KEY).append(message));
	}
}
