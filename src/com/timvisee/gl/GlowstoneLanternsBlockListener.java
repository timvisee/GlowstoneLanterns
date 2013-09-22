package com.timvisee.gl;

import java.util.logging.Logger;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.timvisee.gl.GlowstoneLanterns;

public class GlowstoneLanternsBlockListener implements Listener {
	public static GlowstoneLanterns plugin;

	public static final Logger log = Logger.getLogger("Minecraft");
	
	public GlowstoneLanternsBlockListener(GlowstoneLanterns instance) {
		plugin = instance;
		glowstoneLanternsPrebuiltLanterns = new GlowstoneLanternsPrebuiltLanterns(plugin);
	}
	
	private GlowstoneLanternsPrebuiltLanterns glowstoneLanternsPrebuiltLanterns = new GlowstoneLanternsPrebuiltLanterns(plugin);

	
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		World world = event.getBlock().getWorld();
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if(plugin.isGLEnabled(player)) {
			if(plugin.getConfig().getIntegerList("lanternBuildBlocks").contains(block.getTypeId())) {
				if(!(plugin.isLantern(world, block))) {
					if(plugin.isGLPrebuildEnabled(player) == false) {
						// Add lantern coordinates to list
						Lantern newLantern = plugin.addLanternToList(world, block,
								plugin.getConfig().getInt("lanternDayTypeID", 20), (byte) plugin.getConfig().getInt("lanternDayDataValue", 0),
								plugin.getConfig().getInt("lanternNightTypeID", 89), (byte) plugin.getConfig().getInt("lanternNightDataValue", 0),
								plugin.getConfig().getInt("lanternRainTypeID", 89), (byte) plugin.getConfig().getInt("lanternRainDataValue", 0));
						
						if(plugin.isRaining(world)) {
							newLantern.setState(plugin.getServer(), 3);
						} else if(plugin.isDay(world)) {
							newLantern.setState(plugin.getServer(), 1);
						} else {
							newLantern.setState(plugin.getServer(), 2);
						}
						
						player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.GREEN + "You placed a Glowstone Lantern");
					} else {
						String lanternName = plugin.GLUsersPrebuiltLanterns.get(player);
						glowstoneLanternsPrebuiltLanterns.placePrebuiltLanternFromFile(lanternName, player, block, world);
						player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.GREEN + "You placed a prebuilt Glowstone Lantern");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		World world = event.getPlayer().getWorld();
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		if(plugin.isLantern(world, block)) {
			if(plugin.canDestroyLanterns(player)) {
				plugin.removeLanternFromList(world, block);
				
				if(!(plugin.isLantern(world, block))) {
					player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You destroyed a Glowstone Lantern");
				} else {
					player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "ERROR, Can't delete Glowstone Lantern!");
				}
			} else {
				event.setCancelled(true);
				player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson to destroy lanterns");
			}
		}
	}
}
