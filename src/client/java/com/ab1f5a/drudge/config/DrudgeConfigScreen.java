package com.ab1f5a.drudge.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.Feature;
import com.ab1f5a.drudge.settings.CheckboxSetting;
import com.ab1f5a.drudge.settings.EnumSetting;
import com.ab1f5a.drudge.settings.Setting;
import com.ab1f5a.drudge.settings.SliderSetting;

public final class DrudgeConfigScreen extends OptionsSubScreen
{
	public DrudgeConfigScreen(Screen lastScreen)
	{
		super(lastScreen, DrudgeClient.MC.options,
			Component.translatable("gui.drudge.config.title"));
	}

	@Override
	protected void addOptions()
	{
		for(Feature feature : DrudgeClient.INSTANCE.getModules()
			.getAllModules())
		{
			list.addHeader(feature.getRenderName());
			list.addSmall(List.of(createToggle(feature)));

			for(Setting setting : feature.getSettings().values())
				list.addSmall(List.of(createWidget(setting)));
		}
	}

	private AbstractWidget createToggle(Feature feature)
	{
		CycleButton<Boolean> button = CycleButton
			.onOffBuilder(feature.isEnabled()).displayOnlyValue()
			.create(feature.getDisplayName(),
				(cycleButton, state) -> feature.setEnabled(state));

		applyTooltip(button, feature.getDescription());

		button.active = isInWorld();
		return button;
	}

	private AbstractWidget createWidget(Setting setting)
	{
		if(setting instanceof CheckboxSetting checkbox)
			return createCheckbox(checkbox);

		if(setting instanceof SliderSetting slider)
			return new SettingSlider(slider);

		if(setting instanceof EnumSetting<?> enumSetting)
			return createEnum(enumSetting);

		throw new IllegalStateException(
			"No config widget for " + setting.getClass().getName());
	}

	private AbstractWidget createCheckbox(CheckboxSetting setting)
	{
		CycleButton<Boolean> button = CycleButton
			.onOffBuilder(setting.isChecked())
			.create(label(setting),
				(cycleButton, state) -> setting.setChecked(state));

		applyTooltip(button, setting.getDescription());
		return button;
	}

	private AbstractWidget createEnum(EnumSetting<?> setting)
	{
		Map<String, Component> names = new LinkedHashMap<>();
		List<String> choices = new ArrayList<>();

		for(Enum<?> value : setting.getValues())
		{
			choices.add(value.toString());
			names.put(value.toString(), Setting.enumDisplay(value));
		}

		CycleButton<String> button = CycleButton
			.<String>builder(choice -> names.getOrDefault(choice,
				Component.literal(choice)), setting.getSelected().toString())
			.withValues(choices).create(label(setting),
				(cycleButton, choice) -> setting.setSelected(choice));

		applyTooltip(button, setting.getDescription());
		return button;
	}

	private static Component label(Setting setting)
	{
		return setting.getDisplayName();
	}

	private static void applyTooltip(AbstractWidget widget, Component description)
	{
		if(!description.getString().isEmpty())
			widget.setTooltip(Tooltip.create(description));
	}

	private static boolean isInWorld()
	{
		return DrudgeClient.MC.player != null
			&& DrudgeClient.MC.gameMode != null;
	}

	private static final class SettingSlider extends AbstractSliderButton
	{
		private final SliderSetting setting;

		private SettingSlider(SliderSetting setting)
		{
			super(0, 0, 150, DEFAULT_HEIGHT, Component.empty(),
				setting.getPercentage());
			this.setting = setting;

			updateMessage();
		}

		@Override
		protected void updateMessage()
		{
			setMessage(CommonComponents.optionNameValue(setting.getDisplayName(),
				Component.literal(setting.getValueString())));
		}

		@Override
		protected void applyValue()
		{
			setting.setValue(setting.getMinimum() + value * setting.getRange());

			value = setting.getPercentage();
		}
	}
}
