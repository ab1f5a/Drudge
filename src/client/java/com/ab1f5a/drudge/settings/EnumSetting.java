package com.ab1f5a.drudge.settings;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.network.chat.Component;

import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.util.json.JsonUtils;

public class EnumSetting<T extends Enum<T>> extends Setting
{
	private final T[] values;
	private T selected;
	private final T defaultSelected;

	public EnumSetting(String name, Component description, T[] values,
		T selected)
	{
		super(name, description);
		this.values = Objects.requireNonNull(values);
		this.selected = Objects.requireNonNull(selected);
		defaultSelected = selected;
	}

	public EnumSetting(String name, String descriptionKey, T[] values,
		T selected)
	{
		this(name, Component.translatable(descriptionKey), values, selected);
	}

	public EnumSetting(String name, T[] values, T selected)
	{
		this(name, Component.empty(), values, selected);
	}

	public T[] getValues()
	{
		return values;
	}

	public T getSelected()
	{
		return selected;
	}

	public void setSelected(T selected)
	{
		this.selected = Objects.requireNonNull(selected);
		DrudgeClient.INSTANCE.saveSettings();
	}

	@Override
	public void resetToDefault()
	{
		setSelected(defaultSelected);
	}

	public boolean setSelected(String selected)
	{
		for(T value : values)
		{
			if(!value.toString().equalsIgnoreCase(selected))
				continue;

			setSelected(value);
			return true;
		}

		return false;
	}

	@Override
	public void fromJson(JsonElement json)
	{
		if(!JsonUtils.isString(json))
			return;

		setSelected(json.getAsString());
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(selected.toString());
	}
}
