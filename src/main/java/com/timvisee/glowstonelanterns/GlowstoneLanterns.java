package com.timvisee.glowstonelanterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.io.*;
import java.util.stream.Collectors;

import com.timvisee.glowstonelanterns.lantern.Lantern;
import com.timvisee.glowstonelanterns.lantern.LanternState;
import com.timvisee.glowstonelanterns.lantern.LanternUpdate;
import com.timvisee.glowstonelanterns.listener.BlockListener;
import com.timvisee.glowstonelanterns.permission.PermissionsManager;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GlowstoneLanterns extends JavaPlugin {

    /**
     * The name of the plugin.
     */
    private static final String PLUGIN_NAME = "Glowstone Lanterns";

    /**
     * The version name of the plugin.
     */
    private static final String PLUGIN_VERSION_NAME = "0.7";

    /**
     * The version code of the plugin.
     */
    private static final int PLUGIN_VERSION_CODE = 7;

    /**
     * The plugin instance.
     */
    public static GlowstoneLanterns instance;

    /**
     * The logger of the plugin.
     */
    public static final Logger log = Logger.getLogger("Minecraft");

    /**
     * All plugin listeners.
     */
    private final BlockListener blockListener = new BlockListener(this);

    /**
     * The permissions manager used for Glowstone Lanterns.
     */
    private PermissionsManager permissionsManager;

    /**
     * All user data.
     */
    public final HashMap<Player, ArrayList<Block>> glUsers = new HashMap<>();
    public final HashMap<Player, String> glUsersPrebuiltLanterns = new HashMap<>();

    /**
     * The list of glowstone lanterns.
     */
    public List<Lantern> glLanterns = new ArrayList<>();

    /**
     * The list of lanterns to update.
     */
    public List<LanternUpdate> lanternsToUpdate = new ArrayList<>();

    /**
     * The lantern delay.
     */
    boolean lanternDelayEnabled = true;
    int lanternDelayTime = 5;

    /* Day States;
     * 0 = unknown
     * 1 = day
     * 2 = night
     * 3 = rain
     */
    // TODO: Use an enum here?
    public HashMap<World, LanternState> lastDayState = new HashMap<>();

    /**
     * File paths for various configuration and data files.
     */
    private File lanternsFile = new File("plugins/Glowstone Lanterns/Glowstone Lanterns.txt");
    public File prebuiltLanternsFolder = new File("Glowstone Lanterns/Prebuilt Lanterns");

    /**
     * Constructor.
     */
    public GlowstoneLanterns() {
        // Define the plugin instance
        instance = this;
    }

    /**
     * Called when the plugin is enabled.
     */
    public void onEnable() {
        // Make sure the configuration file exists
        try {
            checkConigFilesExist();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup custom files and folders
        lanternsFile = new File(getDataFolder() + "/" + getConfig().getString("GlowstoneLanternsFile", "Glowstone Lanterns.txt"));
        prebuiltLanternsFolder = new File(getDataFolder() + "/" + getConfig().getString("PrebuiltLanternsFolder", "Prebuilt Lanterns"));

        // Set up the permissions manager
        setUpPermissionsManager();

        // Load lanterns
        loadLanterns();

        // Get the plugin manager instances, to register event listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.blockListener, this);

        // Create new timer to check world times and change lanterns if needed
        // Run a timer that run's the timer() function every 1 second
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::timer, 20, 20);
        // TODO: Make the above time configurable

        // Lantern delay settings
        this.lanternDelayEnabled = getConfig().getBoolean("changeDelayEnabled", true);
        if (getConfig().getInt("changeDelayTime", 5) >= 1)
            this.lanternDelayTime = getConfig().getInt("changeDelayTime", 5);
        else
            log.info("[Glowstone Lanterns] The 'changeDelayTime' property in the config file has to be 1 or above");

        // Start the scheduled task to change delayed lanterns
        // Run a timer to change delayed lanterns
        if(this.lanternDelayEnabled)
            getServer().getScheduler().scheduleSyncRepeatingTask(this, this::updateNextLantern, this.lanternDelayTime, this.lanternDelayTime);

        // The plugin has been enabled, show a status message
        log.info("[" + getPluginName() + "] " + getVersionComplete(true) + " started");
    }

    /**
     * Called when the plugin is disabled.
     */
    public void onDisable() {
        log.info("[" + getPluginName() + "] " + getVersionComplete(true) + " disabled");
    }

    /**
     * Get the name of the plugin.
     *
     * @return Plugin name.
     */
    public static String getPluginName() {
        return PLUGIN_NAME;
    }

    /**
     * Get the current installed Glowstone Lanterns version name.
     *
     * @return The version name of the currently installed Glowstone lanterns instance.
     */
    public static String getVersionName() {
        return PLUGIN_VERSION_NAME;
    }

    /**
     * Get the current installed Dungeon Maze version code.
     *
     * @return The version code of the currently installed Dungeon Maze instance.
     */
    public static int getVersionCode() {
        return PLUGIN_VERSION_CODE;
    }

    /**
     * Get the complete version identifier.
     * The includes a prefixed 'v' sign, the version name and the version code between brackets.
     *
     * @param name True to include the plugin name in front.
     *
     * @return The complete version string.
     */
    public static String getVersionComplete(boolean name) {
        return (name ? PLUGIN_NAME : "") + " v" + getVersionName() + " (" + getVersionCode() + ")";
    }

    /**
     * Set up the permissions manager.
     */
    public void setUpPermissionsManager() {
        // Construct the permissions manager
        this.permissionsManager = new PermissionsManager(this.getServer(), this, this.getLogger());

        // Set up the permissions manager
        this.permissionsManager.setup();
    }

    /**
     * Get the permissions manager instance.
     *
     * @return Permissions manager.
     */
    public PermissionsManager getPermissionsManager() {
        return this.permissionsManager;
    }

    /**
     * Check whether the configuration and data files exist. Create them if that isn't the case.
     *
     * @throws Exception
     */
    public void checkConigFilesExist() throws Exception {
        if (!getDataFolder().exists()) {
            log.info("[Glowstone Lanterns] Creating default files");
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }
        File f = new File(getDataFolder(), "Prebuilt Lanterns");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Creating prebuilt lanterns folder");
            //noinspection ResultOfMethodCallIgnored
            f.mkdirs();
        }

        f = new File(getDataFolder(), "config.yml");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/config.yml"), f);
        }

        f = new File(getDataFolder(), "lanterns.list");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/lanterns.list"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/1.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/1.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/2.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/2.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/3.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/3.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/4.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/4.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/5.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/5.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/6.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/6.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/7.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/7.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/8.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/8.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/ceiling.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/ceiling.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/fireplace.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/fireplace.gllantern"), f);
        }

        f = new File(getDataFolder(), "Prebuilt Lanterns/pumpkin.gllantern");
        if (!f.exists()) {
            log.info("[Glowstone Lanterns] Generating new config file");
            copy(getResource("res/defaultFiles/Glowstone Lanterns/Prebuilt Lanterns/pumpkin.gllantern"), f);
        }
    }

    /**
     * Copy an input stream to a target file. If the file doesn't exist, it will be created.
     *
     * @param in The input stream.
     * @param file The target file.
     */
    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save all the loaded lanterns.
     *
     * @return True if succeed, false if failed.
     */
    public boolean saveLanterns() {
        return saveLanterns(false);
    }

    /**
     * Save the lanterns to the specified file.
     *
     * @param hideMessages True to hide the save message.
     *
     * @return True if succeed, false if failed.
     */
    public boolean saveLanterns(boolean hideMessages) {
        // Save the lanterns list
        if (!hideMessages)
            log.info("[Glowstone Lanterns] Saving Glowstone Lanterns...");

        try {
            // Save and write the Lanterns to an external file
            BufferedWriter out = new BufferedWriter(new FileWriter(lanternsFile));
            for (int i = 0; i < glLanterns.size(); i++) {
                if (i != 0)
                    out.newLine();
                out.write(glLanterns.get(i).getDataString());
            }
            out.close();

            // Lanterns saved, show console message and return true
            if (!hideMessages)
                log.info("[Glowstone Lanterns] " + String.valueOf(countLanterns()) + " Glowstone Lanterns saved!");
            return true;

        } catch (IOException e) {
            // Errir while saving, print error in console
            e.printStackTrace();
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
        if (lanternsFile.exists()) {

            if(!hideMessages)
                log.info("[Glowstone Lanterns] Loading Glowstone Lanterns...");

            File file = lanternsFile;
            FileInputStream fis;
            BufferedInputStream bis;
            DataInputStream dis;

            try {
                // Here BufferedInputStream is added for fast reading.
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                dis = new DataInputStream(bis);

                // Clean array/list first
                glLanterns.clear();

                while(dis.available() != 0)
                    glLanterns.add(new Lantern(dis.readLine()));

                fis.close();
                bis.close();
                dis.close();

                // Lanterns loaded, show message return true
                if(!hideMessages)
                    log.info("[Glowstone Lanterns] " + String.valueOf(countLanterns()) + " Glowstone Lanterns loaded!");
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

    /**
     * Count the number of loaded lanterns.
     *
     * @return The number of loaded lanterns.
     */
    public int countLanterns() {
        return glLanterns.size();
    }

    /**
     * Check whether a world is loaded.
     *
     * @param worldName The name of the world.
     *
     * @return True if the world is loaded, false if not.
     */
    public boolean isWorldLoaded(String worldName) {
        // TODO: Improve the efficiency of this code!
        List<World> worlds = new ArrayList<>();
        List<String> worldNames = new ArrayList<>();

        worlds.addAll(getServer().getWorlds());
        worldNames.addAll(worlds.stream().map(World::getName).collect(Collectors.toList()));
        return worldNames.contains(worldName);
    }

    // Get the facing direction from a player
    public String getFacingDirectionFromPlayer(Player player) {
        Location loc = player.getLocation();
        String facing[] = {"W", "N", "E", "S"};
        double yaw = ((loc.getYaw() + 22.5) % 360);
        if (yaw < 0) yaw += 360;
        return facing[(int) (yaw / 90)];
    }

    /**
     * Get a list of all the worlds.
     *
     * @return List of all worlds.
     */
    // TODO: Show unloaded worlds too, or rename the method to make the returned value more clear?
    public List<World> getAllWorlds() {
        return getServer().getWorlds();
    }

    public void timer() {
        // Check lanterns for every world
        for (World world : getAllWorlds()) {
            // Check if the current world is loaded
            if (isWorldLoaded(world.getName())) {
                // Get current day state of the world
                LanternState currentDayState;
                if(world.hasStorm())
                    currentDayState = LanternState.RAIN;
                else if(isDay(world))
                    currentDayState = LanternState.DAY;
                else
                    currentDayState = LanternState.NIGHT;
                

                // World state not save yet, put unknown state in list
                if (!lastDayState.containsKey(world))
                    lastDayState.put(world, LanternState.UNKNOWN);

                // Check if last state was different
                if (lastDayState.get(world) != currentDayState) {
                    // Last day state was different
                    // Show console message if needed
                    if (currentDayState == LanternState.DAY) {
                        if (getConfig().getBoolean("ShowTimeChangedMessagesInConsole", true))
                            System.out.println("[Glowstone Lanterns] Day in '" + world.getName() + "', set lanterns");
                    } else if (currentDayState == LanternState.NIGHT) {
                        if(getConfig().getBoolean("ShowTimeChangedMessagesInConsole", true))
                            System.out.println("[Glowstone Lanterns] Night in '" + world.getName() + "', set lanterns");
                    } else if (getConfig().getBoolean("ShowTimeChangedMessagesInConsole", true))
                            System.out.println("[Glowstone Lanterns] Rain in '" + world.getName() + "', set lanterns");

                    // Update state in the list
                    lastDayState.remove(world);
                    lastDayState.put(world, currentDayState);

                    if(glLanterns.size() != 0) {
                        glLanterns.stream().filter(l -> l.getWorld().equals(world.getName())).forEach(l -> {

                            if (this.lanternDelayEnabled) {
                                LanternUpdate lu = new LanternUpdate(l, currentDayState);
                                lanternsToUpdate.add(lu);
                            } else
                                l.setState(getServer(), currentDayState);
                        });
                    }

                    saveLanterns(true);
                }
            }
        }
    }

    /**
     * Update the next lantern that is in the update queue.
     */
    public void updateNextLantern() {
        if (this.lanternsToUpdate.size() > 0) {
            this.lanternsToUpdate.get(0).updateLantern(getServer());
            this.lanternsToUpdate.remove(0);
        }
    }

    /**
     * Add a new lantern to the list of lanterns.
     *
     * @param world The world of the lantern.
     * @param block The block of the lantern.
     * @param lanternDayType The type of block of the lantern when it's daylight.
     * @param lanternDayData The data type of the block of the lantern when it's daylight.
     * @param lanternNightType The type of block of the lantern when it's night time.
     * @param lanternNightData The data type of the block of the lantern when it's night time.
     * @param lanternRainType The type of block of the lantern when it's raining.
     * @param lanternRainData The data type of the block of the lantern when it's raining.
     *
     * @return The lantern that has been constructed and added.
     */
    public Lantern addLanternToList(World world, Block block,
                                    int lanternDayType, byte lanternDayData,
                                    int lanternNightType, byte lanternNightData,
                                    int lanternRainType, byte lanternRainData) {
        // Construct the new lantern to add
        Lantern newLantern = new Lantern(world.getName(),
                block.getX(), block.getY(), block.getZ(),
                LanternState.UNKNOWN,
                lanternDayType, lanternDayData,
                lanternNightType, lanternNightData,
                lanternRainType, lanternRainData);

        // Add the lantern to the list
        glLanterns.add(newLantern);

        // Force-save the list of lanterns
        // TODO: Should we do this?
        saveLanterns(true);

        // Return the newly constructed lantern
        return newLantern;
    }

    // Check if a block is a lantern
    public boolean isLantern(World world, Block block) {
        for (Lantern item : glLanterns) {
            if (item.getWorld().equals(world.getName())) {
                if (item.getBlock(getServer()).equals(block)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Remove lantern from list
    public void removeLanternFromList(World world, Block block) {
        List<Lantern> remove;
        remove = new ArrayList<>();
        for (Lantern item : glLanterns) {
            if (item.getWorld().equals(world.getName())) {
                if (item.getBlock(getServer()).equals(block)) {
                    remove.add(item);
                }
            }
        }

        for (Lantern item : remove) {
            glLanterns.remove(item);
        }

        saveLanterns(true);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (commandLabel.equalsIgnoreCase("glowstonelantern") || commandLabel.equalsIgnoreCase("glowstonelanterns") || commandLabel.equalsIgnoreCase("gl")) {

            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You could only use this command in-game");
                    return true;
                }

                if (isGLEnabled((Player) sender)) {
                    togglePlaceFinishedLanterns((Player) sender, "", true, false);
                }
                toggleGL((Player) sender);
                return true;

            } else {
                if (args[0].equalsIgnoreCase("lantern") || args[0].equalsIgnoreCase("l")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You could only use this command in-game");
                        return true;
                    }
                    if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("")) {
                            togglePlaceFinishedLanterns((Player) sender, "", true, true);
                            return true;
                        } else {
                            if (!isGLEnabled((Player) sender)) {
                                toggleGL((Player) sender);
                            }
                            togglePlaceFinishedLanterns((Player) sender, args[1], false, true);
                            return true;
                        }
                    } else {
                        togglePlaceFinishedLanterns((Player) sender, "", true, true);
                        return true;
                    }

                } else if (args[0].equalsIgnoreCase("list")) {
                    if (canUseGLList((Player) sender)) {
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("l") || args[1].equalsIgnoreCase("lantern") || args[1].equalsIgnoreCase("lanterns")) {
                                List<String> prebuiltLanternsList = new ArrayList<>();
                                prebuiltLanternsList.addAll(prebuiltLanternsList());
                                sender.sendMessage(ChatColor.YELLOW + "========== Prebuild Lanterns ==========");
                                String listToShow = "";
                                for (int i = 0; i < prebuiltLanternsList.size(); i++) {
                                    listToShow += ChatColor.WHITE.toString() + prebuiltLanternsList.get(i).replaceAll(".gllantern", "");
                                    if (i < (prebuiltLanternsList.size() - 1)) {
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
                } else if (args[0].equalsIgnoreCase("info")) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You could only use this command in-game");
                        return true;
                    }
                    if (canUseGLInfo((Player) sender)) {
                        if (isGLEnabled((Player) sender)) {
                            sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Glowstone Lanterns " + ChatColor.GREEN + "Enabled");
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Glowstone Lanterns " + ChatColor.DARK_RED + "Disabled");
                        }
                        if (isGLPrebuildEnabled((Player) sender)) {
                            sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Prebuilt Glowstone Lanterns " + ChatColor.GREEN + "Enabled");
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Prebuilt Glowstone Lanterns " + ChatColor.DARK_RED + "Disabled");
                        }
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson");
                        return true;
                    }

                } else if (args[0].equalsIgnoreCase("save")) {
                    if (canUseGLSave((Player) sender)) {

                        sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Saving lanterns...");
                        boolean result = saveLanterns();
                        if (result) {
                            sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.GREEN + String.valueOf(countLanterns()) + " Lanterns succesfully saved!");
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "Error while saving lanterns!");
                        }
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson");
                        return true;
                    }

                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (canUseGLReload((Player) sender)) {
                        boolean result = loadLanterns();
                        if (result) {
                            sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.GREEN + String.valueOf(countLanterns()) + " Lanterns succesfully reloaded!");
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "Error while reloading lanterns!");
                        }
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "You don't have permisson");
                        return true;
                    }

                } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("?")) {
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

                } else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("v")) {
                    PluginDescriptionFile pdfFile = getDescription();
                    sender.sendMessage(ChatColor.YELLOW + "This server is running Glowstone Lanterns v" + pdfFile.getVersion());
                    sender.sendMessage(ChatColor.YELLOW + "Glowstone Lanterns is made by Tim Visee - timvisee.com");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get a list of available prebuilt lanterns.
     *
     * @return List of prebuilt lanterns. An empty list is returned if no prebuilt lanterns are found.
     */
    public List<String> prebuiltLanternsList() {
        File folder = prebuiltLanternsFolder;
        File[] listOfFiles = folder.listFiles();
        List<String> prebuiltLanternsList = new ArrayList<>();

        assert listOfFiles != null;

        for (File listOfFile : listOfFiles)
            if(listOfFile.isFile() && listOfFile.getPath().endsWith(".gllantern"))
                prebuiltLanternsList.add(listOfFile.getName());

        return prebuiltLanternsList;
    }

    /**
     * Toggle the place/build mode of glowstone lanterns for the specified player.
     *
     * @param player The player to toggle the mode for.
     */
    public void toggleGL(Player player) {
        if (isGLEnabled(player)) {
            this.glUsers.remove(player);
            player.sendMessage(ChatColor.YELLOW + "[" + getPluginName() + "] Glowstone Lanterns " + ChatColor.DARK_RED + "Disabled");
        } else {
            if (canUseGL(player)) {
                this.glUsers.put(player, null);
                player.sendMessage(ChatColor.YELLOW + "[" + getPluginName() + "] Glowstone Lanterns " + ChatColor.GREEN + "Enabled");
            } else
                player.sendMessage(ChatColor.YELLOW + "[" + getPluginName() + "] " + ChatColor.DARK_RED + "You don't have permisson");
        }
    }

    // Toggle the 'Place finished lanterns' command
    public void togglePlaceFinishedLanterns(Player player, String lanternName, boolean disablePlaceLanterns, boolean showMessage) {
        if (!disablePlaceLanterns) {
            File lanternFile = new File(prebuiltLanternsFolder + "/" + lanternName + ".gllantern");

            if (lanternFile.exists()) {
                if (glUsersPrebuiltLanterns.containsKey(player))
                    glUsersPrebuiltLanterns.remove(player);

                glUsersPrebuiltLanterns.put(player, lanternName);
                player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Place prebuilt lantern " + ChatColor.WHITE + lanternName + " " + ChatColor.GREEN + "Enabled");
            } else
                player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] " + ChatColor.DARK_RED + "Unknown lantern, try " + ChatColor.WHITE + "/gl list lantern");

        } else {
            if(glUsersPrebuiltLanterns.containsKey(player))
                glUsersPrebuiltLanterns.remove(player);

            if(showMessage)
                player.sendMessage(ChatColor.YELLOW + "[Glowstone Lanterns] Place prebuilt lanterns " + ChatColor.DARK_RED + "Disabled");
        }
    }

    /**
     * Check whether it's day or night time in the specified world.
     *
     * @param world The world.
     *
     * @return True if it's daytime, false if it's nighttime
     */
    public boolean isDay(World world) {
        // Get the world time
        long time = world.getTime();

        // Return true if it's currently day time
        return time < getConfig().getInt("NightStart", 12400) || time > getConfig().getInt("DayStart", 23700);
    }

    /**
     * Check whether the build mode is enabled for the specified player.
     *
     * @param player The player to check for.
     *
     * @return True if the build mode is enable for this player. False if not.
     */
    public boolean isGLEnabled(Player player) {
        return this.glUsers.containsKey(player);
    }

    public boolean isGLPrebuildEnabled(Player player) {
        return this.glUsersPrebuiltLanterns.containsKey(player);
    }

    /**
     * Check whether a player can use glowstone lanterns
     *
     * @param p Player
     * @return True if the player has permission
     */
    public boolean canUseGL(Player p) {
        return this.getPermissionsManager().hasPermission(p, "glowstonelanterns.gl", p.isOp());
    }

    /**
     * Check whether a player can destroy Glowstone Lanterns
     *
     * @param p Player
     * @return True if the player has permission
     */
    public boolean canDestroyLanterns(Player p) {
        return this.getPermissionsManager().hasPermission(p, "glowstonelanterns.destroyLantern", p.isOp());
    }

    /**
     * Check whether a player can use the info command
     *
     * @param p Player
     * @return True if the player has permission
     */
    public boolean canUseGLInfo(Player p) {
        return this.getPermissionsManager().hasPermission(p, "glowstonelanterns.info", p.isOp());
    }

    /**
     * Check whether a player can use the list command
     *
     * @param p Player
     * @return True if the player has permission
     */
    public boolean canUseGLList(Player p) {
        return this.getPermissionsManager().hasPermission(p, "glowstonelanterns.list", p.isOp());
    }

    /**
     * Check whether a player can use the save command
     *
     * @param p Player
     * @return True if the player has permission
     */
    public boolean canUseGLSave(Player p) {
        return this.getPermissionsManager().hasPermission(p, "glowstonelanterns.save", p.isOp());
    }

    /**
     * Check whether a player can use the reload command
     *
     * @param p Player
     * @return True if the player has permission
     */
    public boolean canUseGLReload(Player p) {
        return this.getPermissionsManager().hasPermission(p, "glowstonelanterns.reload", p.isOp());
    }
}
