package com.ab1f5a.drudge.ai;

import net.minecraft.client.Minecraft;
import com.ab1f5a.drudge.DrudgeClient;

public record PlayerAbilities(boolean invulnerable, boolean creativeFlying,
	boolean flying, boolean immuneToFallDamage, boolean noWaterSlowdown,
	boolean jesus, boolean spider)
{

	private static final Minecraft MC = DrudgeClient.MC;

	public static PlayerAbilities get()
	{
		net.minecraft.world.entity.player.Abilities mcAbilities =
			MC.player.getAbilities();

		boolean invulnerable =
			mcAbilities.invulnerable || mcAbilities.instabuild;
		boolean creativeFlying = mcAbilities.flying;

		return new PlayerAbilities(invulnerable, creativeFlying,
			creativeFlying, invulnerable, false, false, false);
	}
}
