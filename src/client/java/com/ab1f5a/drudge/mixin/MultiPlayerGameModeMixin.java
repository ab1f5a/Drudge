package com.ab1f5a.drudge.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.ab1f5a.drudge.event.EventManager;
import com.ab1f5a.drudge.events.BlockBreakingProgressListener.BlockBreakingProgressEvent;
import com.ab1f5a.drudge.mixinterface.IMultiPlayerGameMode;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements IMultiPlayerGameMode
{
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
		method = "continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getId()I",
			ordinal = 0))
	private void onPlayerDamageBlock(BlockPos pos, Direction direction,
		CallbackInfoReturnable<Boolean> cir)
	{
		EventManager.fire(new BlockBreakingProgressEvent(pos, direction));
	}

	@Override
	public void windowClick_QUICK_MOVE(int slot)
	{
		handleContainerInput(0, slot, 0, ContainerInput.QUICK_MOVE,
			minecraft.player);
	}

	@Override
	public void windowClick_SWAP(int from, int to)
	{
		handleContainerInput(0, from, to, ContainerInput.SWAP,
			minecraft.player);
	}

	@Override
	public void rightClickBlock(BlockPos pos, Direction side, Vec3 hitVec)
	{
		BlockHitResult hitResult = new BlockHitResult(hitVec, side, pos, false);
		InteractionHand hand = InteractionHand.MAIN_HAND;
		useItemOn(minecraft.player, hand, hitResult);
		useItem(minecraft.player, hand);
	}

	@Shadow
	public abstract InteractionResult useItemOn(LocalPlayer player,
		InteractionHand hand, BlockHitResult hitResult);

	@Shadow
	public abstract InteractionResult useItem(Player player,
		InteractionHand hand);

	@Shadow
	public abstract void handleContainerInput(int syncId, int slotId,
		int button, ContainerInput actionType, Player player);
}
