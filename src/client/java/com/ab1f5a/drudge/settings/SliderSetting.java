package com.ab1f5a.drudge.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.util.MathUtils;
import com.ab1f5a.drudge.util.json.JsonUtils;

public class SliderSetting extends Setting
{
	private double value;
	private final double defaultValue;
	private final double minimum;
	private final double maximum;
	private final double increment;
	private final ValueDisplay display;

	public SliderSetting(String name, Component description, double value,
		double minimum, double maximum, double increment, ValueDisplay display)
	{
		super(name, description);
		this.value = value;
		defaultValue = value;

		this.minimum = minimum;
		this.maximum = maximum;

		this.increment = increment;
		this.display = display;
	}

	public SliderSetting(String name, String descriptionKey, double value,
		double minimum, double maximum, double increment, ValueDisplay display)
	{
		this(name, Component.translatable(descriptionKey), value, minimum,
			maximum, increment, display);
	}

	public SliderSetting(String name, double value, double minimum,
		double maximum, double increment, ValueDisplay display)
	{
		this(name, Component.empty(), value, minimum, maximum, increment,
			display);
	}

	public final double getValue()
	{
		return MathUtils.clamp(value, minimum, maximum);
	}

	public final double getValueSq()
	{
		return Mth.square(getValue());
	}

	public final int getValueI()
	{
		return (int)getValue();
	}

	public final int getValueCeil()
	{
		return Mth.ceil(getValue());
	}

	public final String getValueString()
	{
		return display.getValueString(getValue());
	}

	public final void setValue(double value)
	{
		value = (int)Math.round(value / increment) * increment;
		value = MathUtils.clamp(value, minimum, maximum);

		this.value = value;
		DrudgeClient.INSTANCE.saveSettings();
	}

	@Override
	public final void resetToDefault()
	{
		setValue(defaultValue);
	}

	public final double getMinimum()
	{
		return minimum;
	}

	public final double getRange()
	{
		return maximum - minimum;
	}

	public final double getPercentage()
	{
		return (getValue() - minimum) / getRange();
	}

	@Override
	public final void fromJson(JsonElement json)
	{
		if(!JsonUtils.isNumber(json))
			return;

		double value = json.getAsDouble();
		if(value > maximum || value < minimum)
			return;

		setValue(value);
	}

	@Override
	public final JsonElement toJson()
	{
		return new JsonPrimitive(Math.round(value * 1e6) / 1e6);
	}

	public static interface ValueDisplay
	{
		public static final ValueDisplay INTEGER = v -> (int)v + "";

		public static final ValueDisplay DECIMAL = v -> {
			String s = Math.round(v * 1e6) / 1e6 + "";
			return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
		};

		public String getValueString(double value);

		public default ValueDisplay withLabelKey(double value, String labelKey)
		{
			return v -> v == value
				? Component.translatable(labelKey).getString()
				: getValueString(v);
		}

		public default ValueDisplay withSuffixKey(String suffixKey)
		{
			return v -> getValueString(v)
				+ Component.translatable(suffixKey).getString();
		}
	}
}
