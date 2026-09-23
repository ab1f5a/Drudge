package com.ab1f5a.drudge;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.events.PostMotionListener;
import com.ab1f5a.drudge.events.PreMotionListener;
import com.ab1f5a.drudge.util.Rotation;
import com.ab1f5a.drudge.util.RotationUtils;

public final class RotationFaker
	implements PreMotionListener, PostMotionListener
{
	private boolean fakeRotation;
	private float serverYaw;
	private float serverPitch;
	private float realYaw;
	private float realPitch;
	
	@Override
	public void onPreMotion()
	{
		if(!fakeRotation)
			return;
		
		LocalPlayer player = DrudgeClient.MC.player;
		realYaw = player.getYRot();
		realPitch = player.getXRot();
		player.setYRot(serverYaw);
		player.setXRot(serverPitch);
	}
	
	@Override
	public void onPostMotion()
	{
		if(!fakeRotation)
			return;
		
		LocalPlayer player = DrudgeClient.MC.player;
		player.setYRot(realYaw);
		player.setXRot(realPitch);
		fakeRotation = false;
	}
	
	public void faceVectorPacket(Vec3 vec)
	{
		Rotation needed = RotationUtils.getNeededRotations(vec);
		LocalPlayer player = DrudgeClient.MC.player;
		
		fakeRotation = true;
		serverYaw =
			RotationUtils.limitAngleChange(player.getYRot(), needed.yaw());
		serverPitch = needed.pitch();
	}
	
	public void faceVectorClient(Vec3 vec)
	{
		Rotation needed = RotationUtils.getNeededRotations(vec);
		
		LocalPlayer player = DrudgeClient.MC.player;
		player.setYRot(
			RotationUtils.limitAngleChange(player.getYRot(), needed.yaw()));
		player.setXRot(needed.pitch());
	}
	
	public void faceVectorClientIgnorePitch(Vec3 vec)
	{
		Rotation needed = RotationUtils.getNeededRotations(vec);
		
		LocalPlayer player = DrudgeClient.MC.player;
		player.setYRot(
			RotationUtils.limitAngleChange(player.getYRot(), needed.yaw()));
		player.setXRot(0);
	}
	
	public float getServerYaw()
	{
		return fakeRotation ? serverYaw : DrudgeClient.MC.player.getYRot();
	}
	
	public float getServerPitch()
	{
		return fakeRotation ? serverPitch : DrudgeClient.MC.player.getXRot();
	}
}
