package com.ab1f5a.drudge.util;

import java.util.function.Predicate;
import java.util.stream.IntStream;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.ab1f5a.drudge.DrudgeClient;
import com.ab1f5a.drudge.mixinterface.IMultiPlayerGameMode;
import com.ab1f5a.drudge.mixinterface.IMinecraftClient;

public enum InventoryUtils
{
	;
	
	private static final Minecraft MC = DrudgeClient.MC;
	private static final IMinecraftClient IMC = DrudgeClient.IMC;
	
	public static int indexOf(Item item)
	{
		return indexOf(stack -> stack.is(item), 36, false);
	}
	
	public static int indexOf(Item item, int maxInvSlot)
	{
		return indexOf(stack -> stack.is(item), maxInvSlot, false);
	}
	
	public static int indexOf(Item item, int maxInvSlot, boolean includeOffhand)
	{
		return indexOf(stack -> stack.is(item), maxInvSlot, includeOffhand);
	}
	
	public static int indexOf(Predicate<ItemStack> predicate)
	{
		return indexOf(predicate, 36, false);
	}
	
	public static int indexOf(Predicate<ItemStack> predicate, int maxInvSlot)
	{
		return indexOf(predicate, maxInvSlot, false);
	}
	
	public static int indexOf(Predicate<ItemStack> predicate, int maxInvSlot,
		boolean includeOffhand)
	{
		return getMatchingSlots(predicate, maxInvSlot, includeOffhand)
			.findFirst().orElse(-1);
	}
	
	public static int count(Item item)
	{
		return count(stack -> stack.is(item), 36, false);
	}
	
	public static int count(Item item, int maxInvSlot)
	{
		return count(stack -> stack.is(item), maxInvSlot, false);
	}
	
	public static int count(Item item, int maxInvSlot, boolean includeOffhand)
	{
		return count(stack -> stack.is(item), maxInvSlot, includeOffhand);
	}
	
	public static int count(Predicate<ItemStack> predicate)
	{
		return count(predicate, 36, false);
	}
	
	public static int count(Predicate<ItemStack> predicate, int maxInvSlot)
	{
		return count(predicate, maxInvSlot, false);
	}
	
	public static int count(Predicate<ItemStack> predicate, int maxInvSlot,
		boolean includeOffhand)
	{
		Inventory inventory = MC.player.getInventory();
		
		return getMatchingSlots(predicate, maxInvSlot, includeOffhand)
			.map(slot -> inventory.getItem(slot).getCount()).sum();
	}
	
	private static IntStream getMatchingSlots(Predicate<ItemStack> predicate,
		int maxInvSlot, boolean includeOffhand)
	{
		Inventory inventory = MC.player.getInventory();
		
		IntStream stream = IntStream.range(0, maxInvSlot);
		if(includeOffhand)
			stream = IntStream.concat(stream, IntStream.of(40));
		
		return stream.filter(i -> predicate.test(inventory.getItem(i)));
	}
	
	public static boolean selectItem(Item item)
	{
		return selectItem(stack -> stack.is(item), 36, false);
	}
	
	public static boolean selectItem(Item item, int maxInvSlot)
	{
		return selectItem(stack -> stack.is(item), maxInvSlot, false);
	}
	
	public static boolean selectItem(Item item, int maxInvSlot,
		boolean takeFromOffhand)
	{
		return selectItem(stack -> stack.is(item), maxInvSlot, takeFromOffhand);
	}
	
	public static boolean selectItem(Predicate<ItemStack> predicate)
	{
		return selectItem(predicate, 36, false);
	}
	
	public static boolean selectItem(Predicate<ItemStack> predicate,
		int maxInvSlot)
	{
		return selectItem(predicate, maxInvSlot, false);
	}
	
	public static boolean selectItem(Predicate<ItemStack> predicate,
		int maxInvSlot, boolean takeFromOffhand)
	{
		return selectItem(indexOf(predicate, maxInvSlot, takeFromOffhand));
	}
	
	public static boolean selectItem(int slot)
	{
		Inventory inventory = MC.player.getInventory();
		IMultiPlayerGameMode im = IMC.getInteractionManager();
		
		if(slot < 0)
			return false;
		
		if(slot < 9)
			inventory.setSelectedSlot(slot);
		else if(inventory.getFreeSlot() > -1 && inventory.getFreeSlot() < 9)
			im.windowClick_QUICK_MOVE(toNetworkSlot(slot));
		else
			im.windowClick_SWAP(toNetworkSlot(slot),
				inventory.getSelectedSlot());
		
		return true;
	}
	
	public static int toNetworkSlot(int slot)
	{
		if(slot >= 0 && slot < 9)
			return slot + 36;
		
		if(slot >= 36 && slot < 40)
			return 44 - slot;
		
		if(slot == 40)
			return 45;
		
		return slot;
	}
	
	public static boolean giveCreativeItem(Item item)
	{
		return giveCreativeItem(new ItemStack(item));
	}
	
	public static boolean giveCreativeItem(ItemStack stack)
	{
		return setCreativeStack(MC.player.getInventory().getFreeSlot(), stack);
	}
	
	public static boolean setCreativeStack(int slot, Item item)
	{
		return setCreativeStack(slot, new ItemStack(item));
	}
	
	public static boolean setCreativeStack(int slot, ItemStack stack)
	{
		if(slot < 0)
			return false;
		
		if(!MC.player.hasInfiniteMaterials())
			return false;
		
		MC.player.getInventory().setItem(slot, stack);
		MC.player.connection.send(new ServerboundSetCreativeModeSlotPacket(
			toNetworkSlot(slot), stack));
		return true;
	}
}
