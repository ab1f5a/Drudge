package com.ab1f5a.drudge.features;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.events.BlockBreakingProgressListener;
import com.ab1f5a.drudge.events.UpdateListener;
import com.ab1f5a.drudge.Feature;
import com.ab1f5a.drudge.mixinterface.IMultiPlayerGameMode;
import com.ab1f5a.drudge.settings.CheckboxSetting;
import com.ab1f5a.drudge.settings.SliderSetting;
import com.ab1f5a.drudge.settings.SliderSetting.ValueDisplay;
import com.ab1f5a.drudge.util.BlockUtils;
import com.ab1f5a.drudge.util.InventoryUtils;

public final class AutoToolSwitch extends Feature
	implements BlockBreakingProgressListener, UpdateListener
{
	private final CheckboxSetting useSwords = new CheckboxSetting("Use swords",
		"description.drudge.setting.autotoolswitch.use_swords", false);

	private final CheckboxSetting useHands = new CheckboxSetting("Use hands",
		"description.drudge.setting.autotoolswitch.use_hands", true);

	private final SliderSetting repairMode = new SliderSetting("Repair mode",
		"description.drudge.setting.autotoolswitch.repair_mode", 0, 0, 100, 1,
		ValueDisplay.INTEGER.withLabelKey(0, "gui.drudge.unit.off"));

	private final CheckboxSetting switchBack = new CheckboxSetting(
		"Switch back", "description.drudge.setting.autotoolswitch.switch_back",
		false);
	
	private int prevSelectedSlot;
	
	public AutoToolSwitch()
	{
		super("AutoToolSwitch");
		
		addSetting(useSwords);
		addSetting(useHands);
		addSetting(repairMode);
		addSetting(switchBack);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(BlockBreakingProgressListener.class, this);
		EVENTS.add(UpdateListener.class, this);
		prevSelectedSlot = -1;
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(BlockBreakingProgressListener.class, this);
		EVENTS.remove(UpdateListener.class, this);

		MC.gui.setOverlayMessage(Component.empty(), false);
	}
	
	private void showActionBar()
	{
		// 同 TreeFeller：每 tick 重发，物品栏上方的提示才不会淡出。
		MC.gui.setOverlayMessage(
			Component.translatable("gui.drudge.hud.autotoolswitch"), false);
	}

	@Override
	public void onBlockBreakingProgress(BlockBreakingProgressEvent event)
	{
		BlockPos pos = event.getBlockPos();
		if(!BlockUtils.canBeClicked(pos))
			return;
		
		if(prevSelectedSlot == -1)
			prevSelectedSlot = MC.player.getInventory().getSelectedSlot();
		
		equipBestTool(pos, useSwords.isChecked(), useHands.isChecked(),
			repairMode.getValueI());
	}
	
	@Override
	public void onUpdate()
	{
		showActionBar();

		if(prevSelectedSlot == -1 || MC.gameMode.isDestroying())
			return;
		
		HitResult hitResult = MC.hitResult;
		if(hitResult != null && hitResult.getType() == HitResult.Type.BLOCK)
			return;
		
		if(switchBack.isChecked())
			MC.player.getInventory().setSelectedSlot(prevSelectedSlot);
		
		prevSelectedSlot = -1;
	}
	
	public void equipIfEnabled(BlockPos pos)
	{
		if(!isEnabled())
			return;
		
		equipBestTool(pos, useSwords.isChecked(), useHands.isChecked(),
			repairMode.getValueI());
	}
	
	public void equipBestTool(BlockPos pos, boolean useSwords, boolean useHands,
		int repairMode)
	{
		LocalPlayer player = MC.player;
		if(player.getAbilities().instabuild)
			return;
		
		ItemStack heldItem = player.getMainHandItem();
		boolean heldItemDamageable = isDamageable(heldItem);
		if(heldItemDamageable && isTooDamaged(heldItem, repairMode))
			putAwayDamagedTool(repairMode);
		
		BlockState state = BlockUtils.getState(pos);
		int bestSlot = getBestSlot(state, useSwords, repairMode);
		if(bestSlot == -1)
		{
			if(useHands && heldItemDamageable && isWrongTool(heldItem, state))
				selectFallbackSlot();
			
			return;
		}
		
		player.getInventory().setSelectedSlot(bestSlot);
	}
	
	private int getBestSlot(BlockState state, boolean useSwords, int repairMode)
	{
		LocalPlayer player = MC.player;
		Inventory inventory = player.getInventory();
		ItemStack heldItem = MC.player.getMainHandItem();
		
		float bestSpeed = getMiningSpeed(heldItem, state);
		if(isTooDamaged(heldItem, repairMode))
			bestSpeed = 1;
		int bestSlot = -1;
		
		for(int slot = 0; slot < 9; slot++)
		{
			if(slot == inventory.getSelectedSlot())
				continue;
			
			ItemStack stack = inventory.getItem(slot);
			
			float speed = getMiningSpeed(stack, state);
			if(speed <= bestSpeed)
				continue;
			
			if(!useSwords && stack.is(ItemTags.SWORDS))
				continue;
			
			if(isTooDamaged(stack, repairMode))
				continue;
			
			bestSpeed = speed;
			bestSlot = slot;
		}
		
		return bestSlot;
	}
	
	private float getMiningSpeed(ItemStack stack, BlockState state)
	{
		float speed = stack.getDestroySpeed(state);
		
		if(speed > 1)
		{
			RegistryAccess drm = DrudgeClient.MC.level.registryAccess();
			Registry<Enchantment> registry =
				drm.lookupOrThrow(Registries.ENCHANTMENT);
			
			Optional<Reference<Enchantment>> efficiency =
				registry.get(Enchantments.EFFICIENCY);
			int effLvl = efficiency.map(entry -> EnchantmentHelper
				.getItemEnchantmentLevel(entry, stack)).orElse(0);
			
			if(effLvl > 0 && !stack.isEmpty())
				speed += effLvl * effLvl + 1;
		}
		
		return speed;
	}
	
	private boolean isDamageable(ItemStack stack)
	{
		return !stack.isEmpty() && stack.isDamageableItem();
	}
	
	private boolean isTooDamaged(ItemStack stack, int repairMode)
	{
		return stack.getMaxDamage() - stack.getDamageValue() <= repairMode;
	}
	
	private void putAwayDamagedTool(int repairMode)
	{
		Inventory inv = MC.player.getInventory();
		int selectedSlot = inv.getSelectedSlot();
		IMultiPlayerGameMode im = IMC.getInteractionManager();
		
		OptionalInt emptySlot = IntStream.range(9, 36)
			.filter(i -> !inv.getItem(i).isEmpty()).findFirst();
		if(emptySlot.isPresent())
		{
			im.windowClick_QUICK_MOVE(
				InventoryUtils.toNetworkSlot(selectedSlot));
			return;
		}
		
		OptionalInt nonDamageableSlot = IntStream.range(9, 36)
			.filter(i -> !isDamageable(inv.getItem(i))).findFirst();
		if(nonDamageableSlot.isPresent())
		{
			im.windowClick_SWAP(nonDamageableSlot.getAsInt(), selectedSlot);
			return;
		}
		
		OptionalInt notTooDamagedSlot = IntStream.range(9, 36)
			.filter(i -> !isTooDamaged(inv.getItem(i), repairMode)).findFirst();
		if(notTooDamagedSlot.isPresent())
		{
			im.windowClick_SWAP(notTooDamagedSlot.getAsInt(), selectedSlot);
			return;
		}
		
		im.windowClick_SWAP(0, selectedSlot);
	}
	
	private boolean isWrongTool(ItemStack heldItem, BlockState state)
	{
		return getMiningSpeed(heldItem, state) <= 1;
	}
	
	private void selectFallbackSlot()
	{
		int fallbackSlot = getFallbackSlot();
		Inventory inventory = MC.player.getInventory();
		
		if(fallbackSlot == -1)
		{
			int prevSlot = inventory.getSelectedSlot();
			if(prevSlot == 8)
				inventory.setSelectedSlot(0);
			else
				inventory.setSelectedSlot(prevSlot + 1);
			
			return;
		}
		
		inventory.setSelectedSlot(fallbackSlot);
	}
	
	private int getFallbackSlot()
	{
		Inventory inventory = MC.player.getInventory();
		
		for(int slot = 0; slot < 9; slot++)
		{
			if(slot == inventory.getSelectedSlot())
				continue;
			
			ItemStack stack = inventory.getItem(slot);
			
			if(!isDamageable(stack))
				return slot;
		}
		
		return -1;
	}
}
