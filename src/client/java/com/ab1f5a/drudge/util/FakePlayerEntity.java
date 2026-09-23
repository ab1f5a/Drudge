package com.ab1f5a.drudge.util;

import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import com.ab1f5a.drudge.DrudgeClient;

public class FakePlayerEntity extends RemotePlayer
{
	private static final Random RANDOM = new Random();
	private final LocalPlayer player = DrudgeClient.MC.player;
	private final ClientLevel world = DrudgeClient.MC.level;
	private PlayerInfo playerListEntry;
	
	public FakePlayerEntity()
	{
		super(DrudgeClient.MC.level, DrudgeClient.MC.player.getGameProfile());
		setId(RANDOM.nextInt(Integer.MIN_VALUE, 0));
		setUUID(UUID.randomUUID());
		copyPosition(player);
		
		copyInventory();
		getAttributes().assignAllValues(player.getAttributes());
		copyRotation();
		
		spawn();
	}
	
	@Override
	protected @Nullable PlayerInfo getPlayerInfo()
	{
		if(playerListEntry == null)
			playerListEntry = Minecraft.getInstance().getConnection()
				.getPlayerInfo(getGameProfile().id());
		
		return playerListEntry;
	}
	
	@Override
	protected void doPush(Entity entity)
	{
	}
	
	private void copyInventory()
	{
		getInventory().replaceWith(player.getInventory());
	}
	
	private void copyRotation()
	{
		yHeadRot = player.yHeadRot;
		yBodyRot = player.yBodyRot;
	}
	
	private void spawn()
	{
		world.addEntity(this);
	}
	
	public void despawn()
	{
		discard();
	}
	
	public void resetPlayerPosition()
	{
		player.snapTo(getX(), getY(), getZ(), getYRot(), getXRot());
	}
}
