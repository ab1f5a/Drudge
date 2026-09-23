package com.ab1f5a.drudge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import com.ab1f5a.drudge.mixinterface.IMinecraftClient;
import com.ab1f5a.drudge.mixinterface.IMultiPlayerGameMode;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IMinecraftClient
{
	@Shadow
	public MultiPlayerGameMode gameMode;

	@Override
	public IMultiPlayerGameMode getInteractionManager()
	{
		return (IMultiPlayerGameMode)gameMode;
	}
}
