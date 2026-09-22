package com.ab1f5a.drudge.settings;

import java.util.Locale;
import java.util.Objects;

import com.google.gson.JsonElement;

import net.minecraft.network.chat.Component;

public abstract class Setting
{
	private final String name;
	private final Component description;

	private String displayKey;

	public Setting(String name, Component description)
	{
		this.name = Objects.requireNonNull(name);
		this.description = Objects.requireNonNull(description);
	}

	public final String getName()
	{
		return name;
	}

	public final void setDisplayKey(String displayKey)
	{
		this.displayKey = displayKey;
	}

	public final Component getDisplayName()
	{
		return displayKey == null ? Component.literal(name)
			: Component.translatableWithFallback(displayKey, name);
	}

	public final Component getDescription()
	{
		return description;
	}

	public static final String slug(String name)
	{
		return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
	}

	public static final String enumKey(Enum<?> value)
	{
		// 必须用 getDeclaringClass：带常量体的枚举（如 FaceTarget.SERVER）
		// 用 getClass() 会拿到匿名子类 FaceTarget$1，键名就错了。
		return "gui.drudge.enum."
			+ value.getDeclaringClass().getSimpleName().toLowerCase(Locale.ROOT)
			+ "." + slug(value.toString());
	}

	public static final Component enumDisplay(Enum<?> value)
	{
		return Component.translatableWithFallback(enumKey(value),
			value.toString());
	}

	public abstract void resetToDefault();
	
	public abstract void fromJson(JsonElement json);
	
	public abstract JsonElement toJson();
}
