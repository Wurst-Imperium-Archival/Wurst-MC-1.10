/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.utils.EntityUtils;
import net.wurstclient.utils.EntityUtils.TargetSettings;

@Mod.Info(
	description = "A bot that follows the closest entity and protects it from other entities.\n"
		+ "Use .protect <entity> to protect a specific entity instead of the closest one.",
	name = "Protect",
	help = "Mods/Protect")
@Mod.Bypasses(ghostMode = false)
public class ProtectMod extends Mod implements UpdateListener
{
	private Entity friend;
	private Entity enemy;
	private float range = 6F;
	private double distanceF = 2D;
	private double distanceE = 3D;
	
	private TargetSettings friendSettingsFind = new TargetSettings()
	{
		@Override
		public boolean targetFriends()
		{
			return true;
		}
		
		@Override
		public float getRange()
		{
			return range;
		}
	};
	
	private TargetSettings friendSettingsKeep = new TargetSettings()
	{
		@Override
		public boolean targetFriends()
		{
			return true;
		}
		
		@Override
		public boolean targetBehindWalls()
		{
			return true;
		};
		
		@Override
		public boolean targetPlayers()
		{
			return true;
		}
		
		@Override
		public boolean targetAnimals()
		{
			return true;
		}
		
		@Override
		public boolean targetMonsters()
		{
			return true;
		}
		
		@Override
		public boolean targetGolems()
		{
			return true;
		}
		
		@Override
		public boolean targetSleepingPlayers()
		{
			return true;
		}
		
		@Override
		public boolean targetInvisiblePlayers()
		{
			return true;
		}
		
		@Override
		public boolean targetInvisibleMobs()
		{
			return true;
		}
		
		@Override
		public boolean targetTeams()
		{
			return false;
		}
	};
	
	private TargetSettings enemySettings = new TargetSettings()
	{
		@Override
		public float getRange()
		{
			return range;
		};
	};
	
	@Override
	public String getRenderName()
	{
		if(friend != null)
			return "Protecting " + friend.getName();
		else
			return "Protect";
	}
	
	@Override
	public void onEnable()
	{
		// set friend
		friend = EntityUtils.getClosestEntity(friendSettingsFind);
		
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// check if player died, friend died or friend disappeared
		if(WMinecraft.getPlayer().getHealth() <= 0
			|| !EntityUtils.isCorrectEntity(friend, friendSettingsKeep))
		{
			friend = null;
			enemy = null;
			setEnabled(false);
			return;
		}
		
		// set enemy
		enemy = EntityUtils.getClosestEntityOtherThan(friend, enemySettings);
		
		// jump if necessary
		if(WMinecraft.getPlayer().isCollidedHorizontally
			&& WMinecraft.getPlayer().onGround)
			WMinecraft.getPlayer().jump();
		
		// swim up if necessary
		if(WMinecraft.getPlayer().isInWater() && WMinecraft
			.getPlayer().posY < (enemy != null ? enemy.posY : friend.posY))
			WMinecraft.getPlayer().motionY += 0.04;
		
		// update timer
		updateMS();
		
		if(enemy == null)
		{
			// follow friend
			EntityUtils.faceEntityClient(friend);
			mc.gameSettings.keyBindForward.pressed =
				WMinecraft.getPlayer().getDistanceToEntity(friend) > distanceF;
		}else
		{
			// follow enemy
			EntityUtils.faceEntityClient(enemy);
			mc.gameSettings.keyBindForward.pressed =
				WMinecraft.getPlayer().getDistanceToEntity(enemy) > distanceE;
			
			// check timer / cooldown
			if(wurst.mods.killauraMod.useCooldown.isChecked()
				? WMinecraft.getPlayer().getCooledAttackStrength(0F) < 1F
				: !hasTimePassedS(wurst.mods.killauraMod.speed.getValueF()))
				return;
			
			// AutoSword
			if(wurst.mods.autoSwordMod.isActive())
				AutoSwordMod.setSlot();
			
			// Criticals
			wurst.mods.criticalsMod.doCritical();
			
			// BlockHit
			wurst.mods.blockHitMod.doBlock();
			
			// attack enemy
			mc.playerController.attackEntity(WMinecraft.getPlayer(), enemy);
			WMinecraft.getPlayer().swingArm(EnumHand.MAIN_HAND);
			
			// reset timer
			updateLastMS();
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		if(friend != null)
			mc.gameSettings.keyBindForward.pressed = false;
	}
	
	public void setFriend(Entity friend)
	{
		this.friend = friend;
	}
}
