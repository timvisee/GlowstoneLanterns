package com.timvisee.glowstonelanterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.io.*;


import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.timvisee.glowstonelanterns.GlowstoneLanternsBlockListener;
import com.timvisee.glowstonelanterns.Lantern;

public class GlowstoneLanterns extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	
	// Listeners
	private final GlowstoneLanternsBlockListener blockListener = new GlowstoneLanternsBlockListener(this);
	
	// User data
	public final HashMap<Player, ArrayList<Block>> GLUsers = new HashMap<Player, ArrayList<Block>>();
	public final HashMap<Player, String> GLUsersPrebuiltLanterns = new HashMap<Player, String>();
	public final HashMap<Player, Boolean> GLUsersCreatePrebuiltLanterns = new HashMap<Player, Boolean>();
	
	// Lanterns
	public List<Lantern> GLLanterns = new ArrayList<Lantern>();
	
	// Lantern updates
	public List<LanternUpdate> lanternUpdates = new ArrayList<LanternUpdate>();
	
	// Lantern delay settings
	boolean lanternDelayEnabled = true;
	int lanternDelayTime = 5;
	
	/* Day States;
	 * 0 = unknown
	 * 1 = day
	 * 2 = night
	 * 3 = rain
	 */
	public HashMap<World, Integer> lastDayState = new HashMap<World, Integer>();
	
	// Setup some default file and folder paths, these will change to the config ones in the onEnable function
	private File lanternsFile = new File("plugins/Glowstone Lanterns/Glowstone Lanterns.txt");
	public File prebuiltLanternsFolder = new File("Glowstone Lanterns/Prebuilt Lanterns");
	
	/*
	 * 0 = none
	 * 1 = PermissionsEx
	 * 2 = PermissionsBukkit
	 * 3 = bPermissions
	 * 4 = Essentials Group Manager
	 * 5 = Permissions
	 */
	private int permissionsSystem = 0;
	private PermissionManager pexPermissions;
	private PermissionHandler defaultPermsissions;
	private GroupManager groupManagerPermissions;

	public void onEnable() {
		// Check if all the config file exists
		try {
			checkConigFilesExist();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
		// Setup costum files and folders
		lanternsFile = new File(getDataFolder() + "/" + getConfig().getString("GlowstoneLanternsFile", "Glowstone Lanterns.txt"));
		prebuiltLanternsFolder = new File(getDataFolder() + "/" + getConfig().getString("PrebuiltLanternsFolder", "Prebuilt Lanterns"));
		
		// Load lanterns
		loadLanterns();
		
		// Register events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.blockListener, this);
		
		// Create new timer to check world times and change lanterns if needed
		getServer().getScheduler().scheduleSyncRepeatingTask( this, new Runnable() { public void run() { timer(); } }, 20, 20); // Run a timer that run's the timer() function every 1 seccond
		
		// Setup permissions
		setupPermissions();
		
		// Lantern delay settings
		this.lanternDelayEnabled = getConfig().getBoolean("changeDelayEnabled", true);
		if(getConfig().getInt("changeDelayTime", 5) >= 1) {
			this.lanternDelayTime = getConfig().getInt("changeDelayTime", 5);
		} else {
			log.info("[Glowstone Lanterns] The 'changeDelayTime' property in the config file has to be 1 or above");
		}
		if(this.lanternDelayEnabled) {
			// Start the scheduled thask to change delayed lanterns
			getServer().getScheduler().scheduleSyncRepeatingTask( this, new Runnable() { public void run() { updateNextLantern(); } }, this.lanternDelayTime, this.lanternDelayTime); // Run a timer to change delayed lanaterns
		}
		
		// Plugin enabled
		PluginDescriptionFile pdfFile = getDescription();
		log.info("[Glowstone Lanterns] Glowstone Lanterns v" + pdfFile.getVersion() + " Started");
	}	

	public void onDisable() {		
		// Plugin disabled
		PluginDescriptionFile pdfFile = getDescription();
		log.info("[Glowstone Lanterns] Glowstone Lanterns v" + pdfFile.getVersion() + " Disabled");
	}
	
	public void checkConigFilesExist() throws Exception {
		if(!getDataFolder().exists()) {
			log.info("[Glowstone Lanterns] Creating default files");
			getDataFolder().mkdirs();
		}
		File f = new File(getDataFolder(), "Prebuilt Lanterns");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Creating prebuilt lanterns folder");
			f.mkdirs();
		}
		
		f = new File(getDataFolder(), "config.yml");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/config.yml"), f);
		}
		
		f = new File(getDataFolder(), "lanterns.list");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/lanterns.list"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/1.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/1.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/2.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/2.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/3.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/3.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/4.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/4.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/5.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/5.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/6.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/6.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/7.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/7.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/8.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/8.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/ceiling.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/ceiling.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/fireplace.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/fireplace.gllantern"), f);
		}
		
		f = new File(getDataFolder(), "Prebuilt Lanterns/pumpkin.gllantern");
		if(!f.exists()) {
			log.info("[Glowstone Lanterns] Generating new config file");
			copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/pumpkin.gllantern"), f);
		}
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void setupPermissions() {
		// Reset permissions
		permissionsSystem = 0;
		
		// Check PermissionsEx system
		Plugin testPex = this.getServer().getPluginManager().getPlugin("PermissionsEx");
		if(testPex != null) {
			pexPermissions = PermissionsEx.getPermissionManager();
			if(pexPermissions != null) {
				permissionsSystem = 1;
				
				System.out.println("[Glowstone Lanterns] Hooked into PermissionsEx!");
				return;
			}
		}
		
		// Check PermissionsBukkit system
		Plugin testBukkitPerms = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
		if(testBukkitPerms != null) {
			permissionsSystem = 2;
			System.out.println("[Glowstone Lanterns] Hooked into PermissionsBukkit!");
			return;
		}
		
		// Check bPermissions system
		/*
		 * Not available yet!
		 */
		
		// Check Essentials Group Manager system
		final PluginManager pluginManager = getServer().getPluginManager();
		final Plugin GMplugin = pluginManager.getPlugin("GroupManager");
		if (GMplugin != null && GMplugin.isEnabled()) {
			permissionsSystem = 4;
			groupManagerPermissions = (GroupManager)GMplugin;
            System.out.println("[Glowstone Lanterns] Hooked into Essentials Group Manager!");
            return;
		}
		
		// Check Permissions system
	    Plugin testPerms = this.getServer().getPluginManager().getPlugin("Permissions");
	    if (this.defaultPermsissions == null) {
	        if (testPerms != null) {
	        	permissionsSystem = 5;
	            this.defaultPermsissions = ((Permissions) testPerms).getHandler();
	            System.out.println("[Glowstone Lanterns] Hooked into Permissions!");
	            return;
	        }
	    }
	    
	    // None of the permissions systems worked >:c.
	    permissionsSystem = 0;
	    System.out.println("[Glowstone Lanterns] No Permissions system found! Permissions disabled!");
	}
	
	public boolean usePermissions() {
		if(permissionsSystem == 0) {
			return false;
		}
		return true;
	}
	
	public int getPermissionsSystem() {
		return permissionsSystem;
	}
	
	public boolean hasPermission(Player player, String permissionNode) {
		return hasPermission(player, permissionNode, player.isOp());
	}
	
	public boolean hasPermission(Player player, String permissionNode, boolean def) {
		if(usePermissions() == false) {
			return def;
		}
		
		// Using PermissionsEx
		if(getPermissionsSystem() == 1) {
			PermissionUser user  = PermissionsEx.getUser(player);
			return user.has(permissionNode);
		}
		
		// Using PermissionsBukkit
		if(getPermissionsSystem() == 2) {
			return player.hasPermission(permissionNode);
		}
		
		// Using bPemissions
		// Available soon!
		
		// Using Essentials Group Manager
		if(getPermissionsSystem() == 4) {
			final AnjoPermissionsHandler handler = groupManagerPermissions.getWorldsHolder().getWorldPermissions(player);
			if (handler == null)
			{
				return false;
			}
			return handler.has(player, permissionNode);
		}
		
		// Using Permissions
		if(getPermissionsSystem() == 5) {
			return this.defaultPermsissions.has(player, permissionNode);
		}

		return false;
	}
	
	public boolean saveLanterns() {
		return saveLanterns(false);
	}
	
	// Save the lanterns to the external file
	public boolean saveLanterns(boolean hideMessages) {
		// Save the lanterns list
		if(!hideMessages){
			log.info("[Glowstone Lanterns] Saving Glowstone Lanterns...");
		}
		
		try {
			// Save and write the Lanterns to an external file
			BufferedWriter out = new BufferedWriter(new FileWriter(lanternsFile));
			for(int i = 0; i < GLLanterns.size(); i++) {
				if(i != 0) { out.newLine(); }
				out.write(GLLanterns.get(i).getDataString());
			}
			out.close();

			// Lanterns saved, show console message and return true
			if(!hideMessages){
				log.info("[Glowstone Lanterns] " + String.valueOf(countLanterns()) + " Glowstone Lanterns saved!");
			}
			return true;
			
		} catch(IOException e) {
			// Errir while saving, print error in console
			System.out.println(e);
			log.info("[Glowstone Lanterns] Error while saving Glowstone Lanterns!");
			return false;
		}
	}
	
	public boolean loadLanterns() {
		return loadLanterns(false);
	}
	
	// Load all the lanterns from the external file
	public boolean loadLanterns(boolean hideMessages) {
		// Load all the lanterns. Check if file exists first
	    if(lanternsFile.exists()) {
	    	
	    	if(!hideMessages) {
	    		log.info("[Glowstone Lanterns] Loading Glowstone Lanterns...");
	    	}
			
	    	File file = lanternsFile;
	        FileInputStream fis = null;
	        BufferedInputStream bis = null;
	        DataInputStream dis = null;

	        try {
				// Here BufferedInputStream is added for fast reading.
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);
				
				// Clean array/list first
				GLLanterns.clear();
				
				while (dis.available() != 0) {
					GLLanterns.add(new Lantern(dis.readLine()));
				}
				fis.close();
				bis.close();
				dis.close();
				
				// Lanterns loaded, show message return true
				if(!hideMessages) {
					log.info("[Glowstone Lanterns] " + String.valueOf(countLanterns()) + " Glowstone Lanterns loaded!");
				}
				return true;
	        } catch (FileNotFoundException e) {
	        	// Something goes wrong with loading the lanterns, show an error message in the console and return false
	        	e.printStackTrace();
	    		log.info("[Glowstone Lanterns] Error by loading Glowstone Lanterns!");
	    		return false;
	        } catch (IOException e) {
	        	// Something goes wrong with loading the lanterns, show an error message in the console and return false
	        	e.printStackTrace();
	    		log.info("[Glowstone Lanterns] Error by loading Glowstone Lanterns!");
	    		return false;
	        }
	    } else {
	    	// The external lanterns file isn't found, show an message in the console and return false
    		log.info("[Glowstone Lanterns] File 'Glowstone Lanterns/Glowstone Lanterns.txt' not found!");
    		return false;
	    }
	}
	
	public int countLanterns() {
		return GLLanterns.size();
	}
	
	// Get config from custom path
	public FileConfiguration getConfigurationFromPath(String filePath, boolean insideDataFolder) {
		if(insideDataFolder) {
			File file = new File(getDataFolder(), filePath);
			return getConfigFromPath(file);
		} else {
			File file = new File(filePath);
			return getConfigFromPath(file);
		}
	}
	
	public FileConfiguration getConfigFromPath(File file) {
		FileConfiguration c;
		
		if (file == null) {
		    return null;
		}

	    c = YamlConfiguration.loadConfiguration(file);
	    
	    return c;
	}

	public String booleanToString(boolean b) {
		return Boolean.toString(b);
	}
	
	// Check if a world is loaded
	public boolean isWorldLoaded(String worldName) {
		List<World> worlds = new ArrayList();
		List<String> worldNames = new ArrayList();
		
		worlds.addAll(getServer().getWorlds());
		for(int i=0; i < worlds.size(); i++) {
			worldNames.add(worlds.get(i).getName());
		}
		if(worldNames.contains(worldName)) {
			return true;
		}
		// No world loaded with this name, return false
		return false;
	}
	
	// Get the facing direction from a player
	public String getFacingDirectionFromPlayer(Player player) {
		Location loc = player.getLocation();
	    String facing[] = {"W", "N", "E", "S"};
	    double yaw = ((loc.getYaw()+22.5) % 360);
	    if (yaw < 0) yaw += 360;
	    return facing[(int)(yaw / 90)];
	}
	
	// Get the facing direction from a player
	public int getFacingDirectionFromPlayerInt(Player player) {
		Location loc = player.getLocation();
	    int facing[] = {0, 1, 2, 3};
	    double yaw = ((loc.getYaw()+22.5) % 360);
	    if (yaw < 0) yaw += 360;
	    return facing[(int)(yaw / 90)];
	}
	
	// Get a list of all worlds
	public List<World> getAllWorlds() {
		return getServer().getWorlds();
	}
	
	
	public void timer() {
		// Check lanterns for every world
		for(World world : getAllWorlds()) {
			
			// Check if the current world is loaded
			if(isWorldLoaded(world.getName())) {
				// Get current day state of the world
				int currentDayState = 0; // 0 = unknown
				if(isRaining(world)) {
					currentDayState = 3; // 3 = raining
				} else if(isDay(world)) {
					currentDayState = 1; // 1 = day
				} else {
					currentDayState = 2; // 2 = night
				}
				
				// World state not save yet, put unknown state in list
				if(lastDayState.containsKey(world) == false) {
					lastDayState.put(world, 0);
				}
				
				// Check if last state was different
				if(lastDayState.get(world) != currentDayState) {
					// Last day state was different
					// Show console message if needed
					if(currentDayState == 1) {
						if(getConfig().getBoolean("ShowTimeChangedMessagesInConsole", true)) {
							System.out.println("[Glowstone Lanterns] Day in '" + world.getName() + "', set lanterns");
						}
					} else if(currentDayState == 2) {
						if(getConfig().getBoolean("ShowTimeChangedMessagesInConsole", true)) {
							System.out.println("[Glowstone Lanterns] Night in '" + world.getName() + "', set lanterns");
						}
					} else {
						if(getConfig().getBoolean("ShowTimeChangedMessagesInConsole", true)) {
							System.out.println("[Glowstone Lanterns] Rain in '" + world.getName() + "', set lanterns");
						}
					}
					
					// Update state in the list
					lastDayState.remove(world);
					lastDayState.put(world, currentDayState);
					
					if(GLLanterns.size() != 0) {
						for(Lantern l : GLLanterns) {
							if(l.getWorld().equals(world.getName())) {
								
								if(this.lanternDelayEnabled) {
									LanternUpdate lu = new LanternUpdate(l, currentDayState);
									lanternUpdates.add(lu);
								} else {
									l.setState(getServer(), currentDayState);
								}
							}
						}
					}
					
					saveLanterns(true);
				}
			}
		}
	}
	
	public void updateNextLantern() {
		if(this.lanternUpdates.size() > 0) {
			this.lanternUpdates.get(0).updateLantern(getServer());
			this.lanternUpdates.remove(0);
		}
	}
	
	public boolean isInt(String string) {
		// Check if a string is an integer
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }
	
	// Add new lantern to lantern list
	public Lantern addLanternToList(World world, Block block,
			int lanternDayType, byte lanternDayData,
			int lanternNightType, byte lanternNightData,
			int lanternRainType, byte lanternRainData) {
		
		Lantern newLantern = new Lantern(world.getName(),
				block.getX(), block.getY(), block.getZ(),
				0,
				lanternDayType, lanternDayData,
				lanternNightType, lanternNightData,
				lanternRainType, lanternRainData);
		
		GLLanterns.add(newLantern);
		
		saveLanterns(true);
		
		return newLantern;
	}
	
	// Check if a block is a lantern
	public boolean isLantern(World world, Block block) {
		for(Lantern item : GLLanterns) {
			if(item.getWorld().equals(world.getName())) {
				if(item.getBlock(getServer()).equals(block)) {
					return true;
				}
			}
		}
		return false;
	}
	
	// Remove lantern from list
	public void removeLanternFromList(World world, Block block) {
		List<Lantern> remove = new ArrayList<Lantern>();
		for(Lantern item : GLLanterns) {
			if(item.getWorld().equals(world.getName())) {
				if(item.getBlock(getServer()).equals(block)) {
					remove.add(item);
				}
			}
		}
		
		for(Lantern item : remove) {
			GLLanterns.remove(item);
		}
		
		saveLanterns(true);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if (commandLabel.equalsIgnoreCase("glowstonelantern") || commandLabel.equalsIgnoreCase("glowstonelanterns") || commandLabel.equalsIgnoreCase("gl")) {
			
			if(args.length == 0) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You could only use this command in-game");
					return true;
				}
				
				if(isGLEnabled((Player) sender)) {
					togglePlaceFinishedLanterns((Player) sender, "", true, false);
				}
				toggleGL((Player) sender);
				return true;
				
			} else {
				if(args[0].equalsIgnoreCase("lantern") || args[0].equalsIgnoreCase("l")) {
					if(!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You could only use this command in-game");
						return true;
					}
					if(args.length == 2) {
						if(args[1].equalsIgnoreCase("")) {
							togglePlaceFinishedLanterns((Player) sender, "", true, true);
							return true;
						} else {
							if(isGLEnabled((Player) sender) == false) {
								toggleGL((Player) sender);
							}
							togglePlaceFinishedLanterns((Player) sender, args[1].toString(), false, true);
							return true;
						}
					} else {
						togglePlaceFinishedLanterns((Player) sender, "", true, true);
						return true;
					}
					
				} else if(args[0].equalsIgnoreCase("list")) {
					if(canUseGLList((Player) sender)) {
						if(args.length == 2) {
							if(args[1].equalsIgnoreCase("l") || args[1].equalsIgnoreCase("lantern") || args[1].equalsIgnoreCase("lanterns")) {
								List<String> prebuiltLanternsList = new ArrayList();
								prebuiltLanternsList.addAll(prebuiltLanternsList());
								sender.sendMessage(ChatColor.YELLOW + "========== Prebuild Lanterns ==========");
								String listToShow = "";
								for(int i=0; i < prebuiltLanternsList.size(); i++) {
									listToShow += ChatColor.WHITE.toString() + prebuiltLanternsList.get(i).replaceAll(".gllantern", "");
									if(i < (prebuiltLanternsList.size() - 1)) {
										listToShow += ChatColor.YELLOW.toString() + ", ";
									}
								}
								sender.sendMessage(listToShow);
								return true;
							}
						}
					} else {
						sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson");
						return true;
					}
				} else if(args[0].equalsIgnoreCase("info")) {
					
					if(!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You could only use this command in-game");
						return true;
					}
					if(canUseGLInfo((Player) sender)) {
						if(isGLEnabled((Player) sender)) {
							sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Glowstone Lanterns " + ChatColor.GREEN + "Enabled");
						} else {
							sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Glowstone Lanterns " + ChatColor.DARK_RED + "Disabled");
						}
						if(isGLPrebuildEnabled((Player) sender)) {
							sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Prebuilt Glowstone Lanterns " + ChatColor.GREEN + "Enabled");
						} else {
							sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Prebuilt Glowstone Lanterns " + ChatColor.DARK_RED + "Disabled");
						}
						return true;
					} else {
						sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson");
						return true;
					}

				} else if(args[0].equalsIgnoreCase("save")) {
					if(canUseGLSave((Player) sender)) {
						
						sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Saving lanterns...");
						boolean result = saveLanterns();
						if(result) {
							sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.GREEN + String.valueOf(countLanterns()) + " Lanterns succesfully saved!");
						} else {
							sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "Error while saving lanterns!");
						}
						return true;
					} else {
						sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson");
						return true;
					}

				} else if(args[0].equalsIgnoreCase("reload")) {
					if(canUseGLReload((Player) sender)) {
						boolean result = loadLanterns();
						if(result) {
							sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.GREEN + String.valueOf(countLanterns()) + " Lanterns succesfully reloaded!");
						} else {
							sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "Error while reloading lanterns!");
						}
						return true;
					} else {
						sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson");
						return true;
					}
				
				} else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("?")) {
					// Show the help
					sender.sendMessage(ChatColor.YELLOW + "========== Glowstone Lanterns Help ==========");
					sender.sendMessage(ChatColor.GOLD + "/glowstonelanterns <help/h/?> " + ChatColor.WHITE + ": View help");
					sender.sendMessage(ChatColor.GOLD + "/glowstonelanterns " + ChatColor.WHITE + ": Enable build mode");
					sender.sendMessage(ChatColor.GOLD + "/glowstonelanterns l [name] " + ChatColor.WHITE + ": Enable prebuild lantern mode");
					sender.sendMessage(ChatColor.GOLD + "/glowstonelanterns list lanterns " + ChatColor.WHITE + ": List prebuild lanterns");
					sender.sendMessage(ChatColor.GOLD + "/glowstonelanterns info " + ChatColor.WHITE + ": Check current status");
					sender.sendMessage(ChatColor.GOLD + "/glowstonelanterns save " + ChatColor.WHITE + ": Save the lanterns");
					sender.sendMessage(ChatColor.GOLD + "/glowstonelanterns reload " + ChatColor.WHITE + ": Reload the lanterns");
					sender.sendMessage(ChatColor.GOLD + "/glowstonelanterns <version/ver/v> " + ChatColor.WHITE + ": Check plugin version");
					return true;
					
				} else if(args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("v")) {
					PluginDescriptionFile pdfFile = getDescription();
					sender.sendMessage(ChatColor.YELLOW + "This server is running Glowstone Lanterns v" + pdfFile.getVersion());
					sender.sendMessage(ChatColor.YELLOW + "Glowstone Lanterns is made by Tim Visee - timvisee.com");
					return true;
				}
			}
		}
		return false;
	}
	
	// Get the list of the lanterns
	public List<String> prebuiltLanternsList() {
		File folder = prebuiltLanternsFolder;
		File[] listOfFiles = folder.listFiles();
		List<String> prebuiltLanternsList = new ArrayList<String>();

	    for (int i = 0; i < listOfFiles.length; i++) {
	    	if (listOfFiles[i].isFile()) {
	    		if(listOfFiles[i].getPath().endsWith(".gllantern")) {
	    			prebuiltLanternsList.add(listOfFiles[i].getName());
	    		}
	    	} else if (listOfFiles[i].isDirectory()) {
	    		// The item is a directory, do nothing
	    	} else {
	    		// File/directory is something else, do nothing
	    	}
	    }
	    
	    return prebuiltLanternsList;
	}

	// Toggle the GL command (place mode command)
	public void toggleGL(Player player) {
		if(isGLEnabled(player)) {
			this.GLUsers.remove(player);
			player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Glowstone Lanterns " + ChatColor.DARK_RED + "Disabled");
		} else {
			if(canUseGL(player)) {
				this.GLUsers.put(player, null);
				player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Glowstone Lanterns " + ChatColor.GREEN + "Enabled");
			} else {
				player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson");
			}
		}
	}
	
	// Toggle the 'Place finished lanterns' command
	public void togglePlaceFinishedLanterns(Player player, String lanternName, boolean disablePlaceLanterns, boolean showMessage) {
		if(disablePlaceLanterns == false) {
			File lanternFile = new File(prebuiltLanternsFolder + "/" + lanternName + ".gllantern");
			
			if(lanternFile.exists()) {
				if(GLUsersPrebuiltLanterns.containsKey(player)) {
					GLUsersPrebuiltLanterns.remove(player);
				}
				GLUsersPrebuiltLanterns.put(player, lanternName);
				player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Place prebuilt lantern " + ChatColor.WHITE + lanternName + " " + ChatColor.GREEN + "Enabled");
			} else {
				player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "Unknown lantern, try " + ChatColor.WHITE + "/gl list lantern");
			}
		} else {
			if(GLUsersPrebuiltLanterns.containsKey(player)) {
				GLUsersPrebuiltLanterns.remove(player);
			}
			if(showMessage == true) {
				player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Place prebuilt lanterns " + ChatColor.DARK_RED + "Disabled");
			}
		}
	}

	/*
	 * Check if a command is enabled
	 */
	public boolean isGLEnabled(Player player) {
		return this.GLUsers.containsKey(player);
	}
	
	public boolean isGLPrebuildEnabled(Player player) {
		return this.GLUsersPrebuiltLanterns.containsKey(player);
	}

	// Check if it's day or night in a world
	public boolean isDay(World world) {
        long time = world.getTime();
        if(time < getConfig().getInt("NightStart", 12400) || time > getConfig().getInt("DayStart", 23700)) {
        	return true;
        } else {
        	return false;
        }
	}
	
	// Check if it's raining (storm) in a world
	public boolean isRaining(World world) {
		if(world.hasStorm()) {
			return true;
		}
		return false;
	}
	
	/*
	 * Permissions getters and setters
	 */
	public boolean canUseGL(Player player) {
	    if (usePermissions()) {
	        return this.hasPermission(player, "glowstonelanterns.gl", player.isOp());
	        
	    }
	    return player.isOp();
	}
	
	public boolean canDestroyLanterns(Player player) {
		if (usePermissions()) {
	        return this.hasPermission(player, "glowstonelanterns.destroyLanterns", player.isOp());
	    }
	    return player.isOp();
	}
	
	public boolean canUseGLInfo(Player player) {
	    if (usePermissions()) {
	        return this.hasPermission(player, "glowstonelanterns.info", player.isOp());
	    }
	    return player.isOp();
	}
	
	public boolean canUseGLList(Player player) {
	    if (usePermissions()) {
	        return this.hasPermission(player, "glowstonelanterns.list", player.isOp());
	    }
	    return player.isOp();
	}

	public boolean canUseGLSave(Player player) {
	    if (usePermissions()) {
	        return this.hasPermission(player, "glowstonelanterns.save", player.isOp());
	    }
	    return player.isOp();
	}

	public boolean canUseGLReload(Player player) {
	    if (usePermissions()) {
	        return this.hasPermission(player, "glowstonelanterns.reload", player.isOp());
	    }
	    return player.isOp();
	}
}
