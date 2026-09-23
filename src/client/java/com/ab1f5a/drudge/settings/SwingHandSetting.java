package com.ab1f5a.drudge.settings;

import java.util.Locale;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.InteractionHand;

import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.Feature;

public final class SwingHandSetting
	extends EnumSetting<SwingHandSetting.SwingHand>
{
	private static final Minecraft MC = DrudgeClient.MC;

	private static final Component DESCRIPTION_SUFFIX =
		buildDescriptionSuffix();

	private SwingHandSetting(Component description, SwingHand[] values,
		SwingHand selected)
	{
		super("Swing hand", description, values, selected);
	}

	public SwingHandSetting(Feature feature, SwingHand selected)
	{
		this(Component.empty().append(featureDescription(feature))
			.append(DESCRIPTION_SUFFIX), SwingHand.values(), selected);
	}

	private static Component featureDescription(Feature feature)
	{
		return Component.translatable("description.drudge.setting."
			+ feature.getName().toLowerCase(Locale.ROOT) + ".swing_hand");
	}

	public void swing(InteractionHand hand)
	{
		getSelected().swing(hand);
	}

	private static Component buildDescriptionSuffix()
	{
		MutableComponent text = Component.literal("\n\n");

		for(SwingHand value : SwingHand.values())
			text.append(Setting.enumDisplay(value)
				.copy().withStyle(ChatFormatting.BOLD))
				.append(Component.literal(" - "))
				.append(value.description)
				.append(Component.literal("\n\n"));

		return text;
	}

	public enum SwingHand
	{
		OFF("Off", hand -> {}),

		SERVER("Server-side",
			hand -> MC.player.connection
				.send(ServerboundPunchPacket.INSTANCE)),

		CLIENT("Client-side",
			hand -> MC.player.swing(hand, SwingAnimation.DEFAULT, true));

		private static final String TRANSLATION_KEY_PREFIX =
			"description.drudge.setting.generic.swing_hand.";

		private final String name;
		private final Component description;
		private final Consumer<InteractionHand> swing;

		private SwingHand(String name, Consumer<InteractionHand> swing)
		{
			this.name = name;
			description = Component.translatable(
				TRANSLATION_KEY_PREFIX + name().toLowerCase(Locale.ROOT));
			this.swing = swing;
		}

		public void swing(InteractionHand hand)
		{
			swing.accept(hand);
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
