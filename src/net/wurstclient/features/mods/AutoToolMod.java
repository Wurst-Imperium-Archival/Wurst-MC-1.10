/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.events.LeftClickEvent;
import net.wurstclient.events.listeners.LeftClickListener;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.Feature;
import net.wurstclient.features.mods.Mod.Bypasses;
import net.wurstclient.features.mods.Mod.Info;

@Info(
	description = "Automatically uses the best tool in your hotbar to\n"
		+ "mine blocks. Tip: This works with Nuker.",
	name = "AutoTool",
	tags = "auto tool",
	help = "Mods/AutoTool")
@Bypasses
public class AutoToolMod extends Mod
	implements LeftClickListener, UpdateListener
{
	private boolean isActive = false;
	private int oldSlot;
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.autoSwordMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(LeftClickListener.class, this);
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!mc.gameSettings.keyBindAttack.pressed && isActive)
		{
			isActive = false;
			mc.thePlayer.inventory.currentItem = oldSlot;
		}else if(isActive && mc.objectMouseOver != null
			&& mc.objectMouseOver.getBlockPos() != null
			&& mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos())
				.getBlock().getMaterial(null) != Material.AIR)
			setSlot(mc.objectMouseOver.getBlockPos());
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(LeftClickListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		isActive = false;
		mc.thePlayer.inventory.currentItem = oldSlot;
	}
	
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		if(mc.objectMouseOver == null
			|| mc.objectMouseOver.getBlockPos() == null)
			return;
		if(mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos())
			.getBlock().getMaterial(null) != Material.AIR)
		{
			isActive = true;
			oldSlot = mc.thePlayer.inventory.currentItem;
			setSlot(mc.objectMouseOver.getBlockPos());
		}
	}
	
	public static void setSlot(BlockPos blockPos)
	{
		float bestSpeed = 1F;
		int bestSlot = -1;
		IBlockState blockState = mc.theWorld.getBlockState(blockPos);
		for(int i = 0; i < 9; i++)
		{
			ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
			if(item == null)
				continue;
			float speed = item.getStrVsBlock(blockState);
			if(speed > bestSpeed)
			{
				bestSpeed = speed;
				bestSlot = i;
			}
		}
		if(bestSlot != -1)
			mc.thePlayer.inventory.currentItem = bestSlot;
	}
}
