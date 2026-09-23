package com.ab1f5a.drudge.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.RotationFaker;
import com.ab1f5a.drudge.DrudgeClient;

public enum RotationUtils
{
	;
	
	private static final Minecraft MC = DrudgeClient.MC;
	
	public static Vec3 getEyesPos()
	{
		LocalPlayer player = MC.player;
		float eyeHeight = player.getEyeHeight(player.getPose());
		return player.position().add(0, eyeHeight, 0);
	}
	
	public static Vec3 getClientLookVec(float partialTicks)
	{
		float yaw = MC.player.getViewYRot(partialTicks);
		float pitch = MC.player.getViewXRot(partialTicks);
		return new Rotation(yaw, pitch).toLookVec();
	}
	
	public static Vec3 getServerLookVec()
	{
		RotationFaker rf = DrudgeClient.INSTANCE.getRotationFaker();
		return new Rotation(rf.getServerYaw(), rf.getServerPitch()).toLookVec();
	}
	
	public static Rotation getNeededRotations(Vec3 vec)
	{
		Vec3 eyes = getEyesPos();
		
		double diffX = vec.x - eyes.x;
		double diffZ = vec.z - eyes.z;
		double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		
		double diffY = vec.y - eyes.y;
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
		double pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ));
		
		return Rotation.wrapped((float)yaw, (float)pitch);
	}
	
	public static double getAngleToLookVec(Vec3 vec)
	{
		LocalPlayer player = MC.player;
		Rotation current = new Rotation(player.getYRot(), player.getXRot());
		Rotation needed = getNeededRotations(vec);
		return current.getAngleTo(needed);
	}
	
	public static float getHorizontalAngleToLookVec(Vec3 vec)
	{
		float currentYaw = Mth.wrapDegrees(MC.player.getYRot());
		float neededYaw = getNeededRotations(vec).yaw();
		return Mth.wrapDegrees(currentYaw - neededYaw);
	}
	
	public static boolean isAlreadyFacing(Rotation rotation)
	{
		return getAngleToLastReportedLookVec(rotation) <= 1.0;
	}
	
	public static double getAngleToLastReportedLookVec(Vec3 vec)
	{
		Rotation needed = getNeededRotations(vec);
		return getAngleToLastReportedLookVec(needed);
	}
	
	public static double getAngleToLastReportedLookVec(Rotation rotation)
	{
		LocalPlayer player = MC.player;
		
		Rotation lastReported = player.isPassenger()
			? new Rotation(player.getYRot(), player.getXRot())
			: new Rotation(player.yRotLast, player.xRotLast);
		
		return lastReported.getAngleTo(rotation);
	}
	
	public static boolean isFacingBox(AABB box, double range)
	{
		Vec3 start = getEyesPos();
		Vec3 end = start.add(getServerLookVec().scale(range));
		return box.clip(start, end).isPresent();
	}
	
	public static Rotation slowlyTurnTowards(Rotation end, float maxChange)
	{
		LocalPlayer player = MC.player;
		
		float startYaw =
			player.isPassenger() ? player.getYRot() : player.yRotLast;
		float startPitch =
			player.isPassenger() ? player.getXRot() : player.xRotLast;
		float endYaw = end.yaw();
		float endPitch = end.pitch();
		
		float yawChange = Math.abs(Mth.wrapDegrees(endYaw - startYaw));
		float pitchChange = Math.abs(Mth.wrapDegrees(endPitch - startPitch));
		
		float maxChangeYaw = pitchChange == 0 ? maxChange
			: Math.min(maxChange, maxChange * yawChange / pitchChange);
		float maxChangePitch = yawChange == 0 ? maxChange
			: Math.min(maxChange, maxChange * pitchChange / yawChange);
		
		float nextYaw = limitAngleChange(startYaw, endYaw, maxChangeYaw);
		float nextPitch =
			limitAngleChange(startPitch, endPitch, maxChangePitch);
		
		return new Rotation(nextYaw, nextPitch);
	}
	
	public static float limitAngleChange(float current, float intended,
		float maxChange)
	{
		float currentWrapped = Mth.wrapDegrees(current);
		float intendedWrapped = Mth.wrapDegrees(intended);
		
		float change = Mth.wrapDegrees(intendedWrapped - currentWrapped);
		change = Mth.clamp(change, -maxChange, maxChange);
		
		return current + change;
	}
	
	public static float limitAngleChange(float current, float intended)
	{
		float currentWrapped = Mth.wrapDegrees(current);
		float intendedWrapped = Mth.wrapDegrees(intended);
		
		float change = Mth.wrapDegrees(intendedWrapped - currentWrapped);
		
		return current + change;
	}
}
