package com.timvisee.glowstonelanterns;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GlowstoneLanternsPrebuiltLanterns {
	public static GlowstoneLanterns plugin;
	public static final Logger log = Logger.getLogger("Minecraft");
	
	public GlowstoneLanternsPrebuiltLanterns(GlowstoneLanterns instance) {
		plugin = instance;
	}
	
	public boolean isStringBoolean(String string) {
        if(string.equalsIgnoreCase("true")) {
        	return true;
        } else if(string.equalsIgnoreCase("false")) {
        	return true;
        }
        return false;
    }
	
	public boolean stringToBoolean(String string) {
		if(string.equalsIgnoreCase("true")) {
        	return true;
        } else if(string.equalsIgnoreCase("false")) {
        	return false;
        }
        return false;
	}
	
	public boolean isStringInt(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }
	
	public int stringToInt(String string) {
		try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return 0;
        }
	}
	
	public String getFacingDirectionFromPlayer(Player player) {
		Location loc = player.getLocation();
	    String facing[] = {"W", "N", "E", "S"};
	    double yaw = ((loc.getYaw()+22.5) % 360);
	    if (yaw < 0) yaw += 360;
	    return facing[(int)(yaw / 90)];
	}
	public int[] convertLocationToFaceLocation(int locX, int locY, int locZ, Player player) {
		int[] convertedLocation = {0, locY, 0};
		
		if(plugin.getFacingDirectionFromPlayer(player) == "N") {
			convertedLocation[0] = locZ * -1;
			convertedLocation[2] = locX * -1;
		} else if(plugin.getFacingDirectionFromPlayer(player) == "E") {
			convertedLocation[0] = locX;
			convertedLocation[2] = locZ * -1;
		} else if(plugin.getFacingDirectionFromPlayer(player) == "S") {
			convertedLocation[0] = locZ;
			convertedLocation[2] = locX;
		} else if(plugin.getFacingDirectionFromPlayer(player) == "W") {
			convertedLocation[0] = locX * -1;
			convertedLocation[2] = locZ;
		}
		
		return convertedLocation;
	}
	
	public List<String> loadPrebuiltLantern(String file, boolean insideLanternsFolder) {
		File lanternFile = new File(plugin.prebuiltLanternsFolder.getPath() + "/" + file);
		List<String> fileContent = new ArrayList<String>();
		
		if(insideLanternsFolder) {
			lanternFile = new File(plugin.prebuiltLanternsFolder.getAbsolutePath() + "/" + file);
		} else {
			lanternFile = new File(file);
		}
		
	    if(lanternFile.exists()) {
	        FileInputStream fis = null;
	        BufferedInputStream bis = null;
	        DataInputStream dis = null;

	        try {
				fis = new FileInputStream(lanternFile.getPath());
				
				// Here BufferedInputStream is added for fast reading.
				bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);
				
				// dis.available() returns 0 if the file does not have more lines.
				while (dis.available() != 0) {
					fileContent.add(dis.readLine());
				}
				
				fis.close();
				bis.close();
				dis.close();
				
				// Lantern data succesfully loaded, return the data
				return fileContent;
	        } catch (FileNotFoundException e) {
	        	e.printStackTrace();
	    		log.info("[Glowstone Lanterns] Error by loading prebuilt glowstone lantern!");
	        } catch (IOException e) {
	        	e.printStackTrace();
	    		log.info("[Glowstone Lanterns] Error by loading prebuilt glowstone lantern!");
	        }
	    } else {
    		log.info("[Glowstone Lanterns] Prebuilt glowstone lantern file not found");
	    }
	    
	    // Something goes wrong by loading the lantern, return NULL
	    return null;
	}
	
	public void placePrebuiltLanternFromFile(String lanternName, Player player, Block onBlock, World world) {
		List<String> lanternData = new ArrayList<String>();
		lanternData = loadPrebuiltLantern(lanternName + ".gllantern", true);
		boolean setError = false;
		
		for(int i = 0; i < lanternData.size(); i++) {
			
			String[] lanternDataValues = lanternData.get(i).split(";");
			
			if(lanternDataValues[0].startsWith("#")) {
				// ignore this row, this is a comment row
			} else if(lanternDataValues[0].replaceAll(" ", "").equalsIgnoreCase("")) {
				// ignore this row
				
			} else if(isStringInt(lanternDataValues[2])
					&& isStringInt(lanternDataValues[3])
					&& isStringInt(lanternDataValues[4])
					&& isStringInt(lanternDataValues[5])
					&& isStringInt(lanternDataValues[6])
					&& isStringInt(lanternDataValues[7])) {
				
					String[] splittedLocation = lanternDataValues[0].split(",");
					
					if(isStringInt(splittedLocation[0]) && isStringInt(splittedLocation[1]) && isStringInt(splittedLocation[2])) {
						
						if(isStringBoolean(lanternDataValues[1])) {
							
							int[] convertedLocationToFace = convertLocationToFaceLocation(stringToInt(splittedLocation[0]), stringToInt(splittedLocation[1]), stringToInt(splittedLocation[2]), player);
							Block currentBlock = world.getBlockAt(onBlock.getX() + convertedLocationToFace[0], onBlock.getY() + convertedLocationToFace[1], onBlock.getZ() + convertedLocationToFace[2]);
							
							if(stringToBoolean(lanternDataValues[1])) {
								Lantern newLantern = plugin.addLanternToList(world, currentBlock,
										stringToInt(lanternDataValues[2]), (byte) stringToInt(lanternDataValues[3]),
										stringToInt(lanternDataValues[4]), (byte) stringToInt(lanternDataValues[5]),
										stringToInt(lanternDataValues[6]), (byte) stringToInt(lanternDataValues[7]));
								
								if(plugin.isRaining(world)) {
									newLantern.setState(plugin.getServer(), LanternState.RAIN);
								} else if(plugin.isDay(world)) {
									newLantern.setState(plugin.getServer(), LanternState.DAY);
								} else {
									newLantern.setState(plugin.getServer(), LanternState.NIGHT);
								}
							} else {
								// change block if it isn't a lantern block
								currentBlock.setTypeId(stringToInt(lanternDataValues[2]));
								currentBlock.setData((byte) stringToInt(lanternDataValues[3]));
							}
							
						} else {
							setError = true;
						}
				} else {
					setError = true;
				}																
			} else {
				// This line is something unknown, setError to true
				setError = true;
			}
			
			// Check if there was an error 
			if(setError) {
				// There was an error found, show a error message
				player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.RED + "Error by loading prebuilt lantern");
				log.info("[Glowstone Lanterns] Error by loading prebuilt lantern");
			}
		}
	}
}
