package com.ab1f5a.drudge.keybinds;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import com.ab1f5a.drudge.Feature;
import com.ab1f5a.drudge.config.DrudgeConfigScreen;
import com.ab1f5a.drudge.module.ModuleList;

public final class DrudgeKeybinds
{
	/** This version still takes the keybind category as a translation key. */
	private static final String CATEGORY = "key.categories.drudge";

	private static DrudgeKeybinds instance;
	private static boolean ticking;

	public static final String OPEN_CONFIG_NAME = "key.drudge.open_config";

	private final KeyMapping openConfig;
	private final Map<Feature, KeyMapping> toggles = new LinkedHashMap<>();

	public DrudgeKeybinds(ModuleList modules)
	{
		// 必须在客户端初始化阶段构造，早于原版“控制”界面加载，否则键位
		// 不会出现在按键绑定里、也无法改键。构造器只靠副作用，通过自身
		// 的静态 instance 保活，所以返回值可以不要。
		instance = this;

		openConfig = register(OPEN_CONFIG_NAME, InputConstants.KEY_RSHIFT);

		for(Feature feature : modules.getAllModules())
			toggles.put(feature,
				register(toggleName(feature), InputConstants.UNKNOWN.getValue()));

		if(!ticking)
		{
			ticking = true;
			ClientTickEvents.END_CLIENT_TICK
				.register(DrudgeKeybinds::onEndTick);
		}
	}

	private static KeyMapping register(String name, int keyCode)
	{
		return KeyBindingHelper.registerKeyBinding(
			new KeyMapping(name, keyCode, CATEGORY));
	}

	public static String toggleName(Feature feature)
	{
		return "key.drudge.toggle."
			+ feature.getName().toLowerCase(Locale.ROOT);
	}

	private static void onEndTick(Minecraft mc)
	{
		if(instance != null)
			instance.poll(mc);
	}

	private void poll(Minecraft mc)
	{
		if(mc.screen != null || mc.getOverlay() != null)
			return;

		while(openConfig.consumeClick())
			mc.setScreen(new DrudgeConfigScreen(mc.screen));

		if(mc.player == null || mc.level == null || mc.gameMode == null)
			return;

		toggles.forEach((feature, mapping) -> {
			while(mapping.consumeClick())
				feature.doPrimaryAction();
		});
	}
}
