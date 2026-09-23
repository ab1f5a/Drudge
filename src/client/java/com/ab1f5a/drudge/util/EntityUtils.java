package com.ab1f5a.drudge.util;

import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.DrudgeClient;

public enum EntityUtils
{
	;
	
	protected static final DrudgeClient DRUDGE = DrudgeClient.INSTANCE;
	protected static final Minecraft MC = DrudgeClient.MC;
	
	public static Stream<Entity> getEntities()
	{
		return StreamSupport
			.stream(MC.level.entitiesForRendering().spliterator(), false);
	}
	
	public static <E extends Entity> Stream<E> getEntities(Class<E> entityClass)
	{
		return getEntities().filter(entityClass::isInstance)
			.map(entityClass::cast);
	}
	
	public static Stream<Entity> getAliveEntities()
	{
		return getEntities().filter(Entity::isAlive);
	}
	
	public static <E extends Entity> Stream<E> getAliveEntities(
		Class<E> entityClass)
	{
		return getEntities(entityClass).filter(Entity::isAlive);
	}
	
	public static Stream<Entity> getFollowableEntities()
	{
		return getAliveEntities().filter(IS_NOT_SELF).filter(
			e -> e instanceof LivingEntity || e instanceof AbstractMinecart);
	}
	
	public static final Predicate<Entity> IS_NOT_SELF =
		e -> e != null && e != MC.player && !(e instanceof FakePlayerEntity);
	
	public static Stream<Entity> getAttackableEntities()
	{
		return getEntities().filter(IS_ATTACKABLE);
	}
	
	public static Stream<LivingEntity> getExplosionWorthyAttackableEntities()
	{
		return getEntities(LivingEntity.class).filter(IS_ATTACKABLE);
	}
	
	public static final Predicate<Entity> IS_ATTACKABLE =
		e -> e != null && e.isAlive()
			&& (e instanceof LivingEntity || e instanceof EndCrystal
				|| e instanceof ShulkerBullet)
			&& IS_NOT_SELF.test(e);
	
	public static Vec3 getLerpedPos(Entity e, float partialTicks)
	{
		if(e.isRemoved())
			return e.position();
		
		double x = Mth.lerp(partialTicks, e.xOld, e.getX());
		double y = Mth.lerp(partialTicks, e.yOld, e.getY());
		double z = Mth.lerp(partialTicks, e.zOld, e.getZ());
		return new Vec3(x, y, z);
	}
	
	public static AABB getLerpedBox(Entity e, float partialTicks)
	{
		if(e.isRemoved())
			return e.getBoundingBox();
		
		Vec3 offset = getLerpedPos(e, partialTicks).subtract(e.position());
		return e.getBoundingBox().move(offset);
	}
	
	public static double distanceToHitboxSq(Entity e)
	{
		Vec3 start = RotationUtils.getEyesPos();
		AABB box = e.getBoundingBox();
		double x = Mth.clamp(start.x, box.minX, box.maxX);
		double y = Mth.clamp(start.y, box.minY, box.maxY);
		double z = Mth.clamp(start.z, box.minZ, box.maxZ);
		return start.distanceToSqr(new Vec3(x, y, z));
	}
	
	public static EntityHitResult createHitResult(Entity e)
	{
		AABB box = e.getBoundingBox();
		Vec3 start = RotationUtils.getEyesPos();
		Vec3 end = box.getCenter();
		Vec3 hitVec = box.clip(start, end).orElse(start);
		return new EntityHitResult(e, hitVec);
	}
}
