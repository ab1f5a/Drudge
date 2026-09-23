package com.ab1f5a.drudge.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.network.chat.Component;

import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.util.json.JsonUtils;

public class CheckboxSetting extends Setting
{
	private boolean checked;
	private final boolean checkedByDefault;

	public CheckboxSetting(String name, Component description,
		boolean checked)
	{
		super(name, description);
		this.checked = checked;
		checkedByDefault = checked;
	}

	public CheckboxSetting(String name, String descriptionKey, boolean checked)
	{
		this(name, Component.translatable(descriptionKey), checked);
	}

	public CheckboxSetting(String name, boolean checked)
	{
		this(name, Component.empty(), checked);
	}

	public final boolean isChecked()
	{
		return checked;
	}

	public final void setChecked(boolean checked)
	{
		this.checked = checked;
		DrudgeClient.INSTANCE.saveSettings();
	}

	@Override
	public final void resetToDefault()
	{
		setChecked(checkedByDefault);
	}

	@Override
	public final void fromJson(JsonElement json)
	{
		if(!JsonUtils.isBoolean(json))
			return;

		setChecked(json.getAsBoolean());
	}

	@Override
	public final JsonElement toJson()
	{
		return new JsonPrimitive(checked);
	}
}
