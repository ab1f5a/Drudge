package com.ab1f5a.drudge;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.mixinterface.IMinecraftClient;
import com.ab1f5a.drudge.module.DontSaveState;
import com.ab1f5a.drudge.settings.Setting;

public abstract class Feature
{
	protected static final DrudgeClient DRUDGE = DrudgeClient.INSTANCE;
	protected static final EventManager EVENTS = DRUDGE.getEventManager();
	protected static final Minecraft MC = DrudgeClient.MC;
	protected static final IMinecraftClient IMC = DrudgeClient.IMC;

	private final String name;
	private final String descriptionKey;

	private boolean enabled;

	// 带 @DontSaveState 的功能（如 TreeFeller）不写 enabled-features.json，
	// 否则每次启动都会被自动开启。此判断不可删除。
	private final boolean stateSaved =
		!getClass().isAnnotationPresent(DontSaveState.class);

	private final LinkedHashMap<String, Setting> settings =
		new LinkedHashMap<>();

	public Feature(String name)
	{
		this.name = Objects.requireNonNull(name);

		if(name.contains(" "))
			throw new IllegalArgumentException(
				"Feature name must not contain spaces: " + name);

		descriptionKey =
			"description.drudge.feature." + name.toLowerCase(Locale.ROOT);
	}

	public final String getName()
	{
		return name;
	}

	public final Component getDisplayName()
	{
		return Component
			.translatable("gui.drudge.feature." + name.toLowerCase(Locale.ROOT));
	}

	public Component getRenderName()
	{
		return getDisplayName();
	}

	public final Component getDescription()
	{
		return Component.translatable(descriptionKey);
	}

	public final boolean isEnabled()
	{
		return enabled;
	}

	public final void setEnabled(boolean enabled)
	{
		if(this.enabled == enabled)
			return;

		this.enabled = enabled;

		if(enabled)
			onEnable();
		else
			onDisable();

		if(stateSaved)
			DRUDGE.getModules().saveEnabledModules();
	}

	public final void doPrimaryAction()
	{
		setEnabled(!enabled);
	}

	public final boolean isStateSaved()
	{
		return stateSaved;
	}

	protected void onEnable()
	{

	}

	protected void onDisable()
	{

	}

	public final Map<String, Setting> getSettings()
	{
		return Collections.unmodifiableMap(settings);
	}

	protected final void addSetting(Setting setting)
	{
		// settings.json 的身份键：保持小写原名，不翻译、不 slug 化，
		// 否则已有存档的键对不上，会静默读不回设置。
		String key = setting.getName().toLowerCase(Locale.ROOT);

		if(settings.containsKey(key))
			throw new IllegalArgumentException(
				"Duplicate setting: " + getName() + " " + key);

		// 显示名走另一条 slug 化后的翻译键，与上面的身份键互不干扰。
		setting.setDisplayKey("gui.drudge.setting."
			+ name.toLowerCase(Locale.ROOT) + "." + Setting.slug(setting.getName()));

		settings.put(key, setting);
	}
}
