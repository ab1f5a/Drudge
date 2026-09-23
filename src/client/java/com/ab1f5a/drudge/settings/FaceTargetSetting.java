package com.ab1f5a.drudge.settings;

import java.util.Locale;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.Feature;

public final class FaceTargetSetting
	extends EnumSetting<FaceTargetSetting.FaceTarget>
{
	private static final DrudgeClient DRUDGE = DrudgeClient.INSTANCE;

	private static final Component DESCRIPTION_SUFFIX =
		buildDescriptionSuffix();

	private FaceTargetSetting(Component description, FaceTarget[] values,
		FaceTarget selected)
	{
		super("Face target", description, values, selected);
	}

	public static FaceTargetSetting withoutPacketSpam(Feature feature,
		FaceTarget selected)
	{
		return new FaceTargetSetting(
			Component.empty().append(featureDescription(feature))
				.append(DESCRIPTION_SUFFIX),
			FaceTarget.values(), selected);
	}

	private static Component featureDescription(Feature feature)
	{
		return Component.translatable("description.drudge.setting."
			+ feature.getName().toLowerCase(Locale.ROOT) + ".face_target");
	}

	public void face(Vec3 v)
	{
		getSelected().face(v);
	}

	private static Component buildDescriptionSuffix()
	{
		MutableComponent text = Component.literal("\n\n");

		for(FaceTarget value : FaceTarget.values())
			text.append(Setting.enumDisplay(value)
				.copy().withStyle(ChatFormatting.BOLD))
				.append(Component.literal(" - "))
				.append(value.description)
				.append(Component.literal("\n\n"));

		return text;
	}

	public enum FaceTarget
	{
		OFF("Off", v -> {}),

		SERVER("Server-side",
			v -> DRUDGE.getRotationFaker().faceVectorPacket(v)),

		CLIENT("Client-side",
			v -> DRUDGE.getRotationFaker().faceVectorClient(v));

		private static final String TRANSLATION_KEY_PREFIX =
			"description.drudge.setting.generic.face_target.";

		private final String name;
		private final Component description;
		private final Consumer<Vec3> face;

		private FaceTarget(String name, Consumer<Vec3> face)
		{
			this.name = name;
			description = Component.translatable(
				TRANSLATION_KEY_PREFIX + name().toLowerCase(Locale.ROOT));
			this.face = face;
		}

		public void face(Vec3 v)
		{
			face.accept(v);
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
