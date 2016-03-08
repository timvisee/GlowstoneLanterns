package com.timvisee.glowstonelanterns.command.executable;

import com.timvisee.glowstonelanterns.GlowstoneLanterns;
import com.timvisee.glowstonelanterns.command.CommandParts;
import com.timvisee.glowstonelanterns.command.ExecutableCommand;
import com.timvisee.glowstonelanterns.permission.PermissionsManager;
import com.timvisee.glowstonelanterns.util.MinecraftUtils;
import com.timvisee.glowstonelanterns.util.SystemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusCommand extends ExecutableCommand {

    /**
     * Execute the command.
     *
     * @param sender           The command sender.
     * @param commandReference The command reference.
     * @param commandArguments The command arguments.
     *
     * @return True if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean executeCommand(CommandSender sender, CommandParts commandReference, CommandParts commandArguments) {
        // Print the status info header
        sender.sendMessage(ChatColor.GOLD + "==========[ " + GlowstoneLanterns.getPluginName().toUpperCase() + " STATUS ]==========");

        // Get the dungeon chunk grid manager
        sender.sendMessage(ChatColor.GOLD + "Loaded Lanterns: " + ChatColor.WHITE + GlowstoneLanterns.instance.glLanterns.size());

        // Get the permissions manager
        PermissionsManager permissionsManager = GlowstoneLanterns.instance.getPermissionsManager();

        // Print the permissions manager status
        if(permissionsManager != null) {
            // Get the used permissions system
            PermissionsManager.PermissionsSystemType type = permissionsManager.getUsedPermissionsSystemType();

            if(type != null)
                sender.sendMessage(ChatColor.GOLD + "Permissions System: " + ChatColor.GREEN + permissionsManager.getUsedPermissionsSystemType().getName());
            else
                sender.sendMessage(ChatColor.GOLD + "Permissions System: " + ChatColor.GRAY + ChatColor.ITALIC + "None");
        } else
            sender.sendMessage(ChatColor.GOLD + "Permissions System: " + ChatColor.DARK_RED + ChatColor.ITALIC + "Unknown!");

        // Print the plugin runtime
        printPluginRuntime(sender);

        // Show the version status
        sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.WHITE + GlowstoneLanterns.getPluginName() + " v" + GlowstoneLanterns.getVersionName() + ChatColor.GRAY + " (code: " + GlowstoneLanterns.getVersionCode() + ")");

        // Print the server status
        printServerStatus(sender);

        // Print the machine status
        printMachineStatus(sender);
        return true;
    }

    /**
     * Print the plugin runtime.
     *
     * @param sender Command sender to print the runtime to.
     */
    public void printPluginRuntime(CommandSender sender) {
        // Get the runtime
        long runtime = new Date().getTime() - GlowstoneLanterns.instance.getInitializationTime().getTime();

        // Calculate the timings
        int millis = (int) (runtime % 1000);
        runtime/=1000;
        int seconds = (int) (runtime % 60);
        runtime/=60;
        int minutes = (int) (runtime % 60);
        runtime/=60;
        int hours = (int) runtime;

        // Create a double and triple digit formatter
        DecimalFormat doubleDigit = new DecimalFormat("######00");
        DecimalFormat tripleDigit = new DecimalFormat("000");

        // Generate the timing string
        StringBuilder runtimeStr = new StringBuilder(ChatColor.WHITE + doubleDigit.format(seconds) + ChatColor.GRAY + "." + ChatColor.WHITE + tripleDigit.format(millis));
        String measurement = "Seconds";
        if(minutes > 0 || hours > 0) {
            runtimeStr.insert(0, ChatColor.WHITE + doubleDigit.format(minutes) + ChatColor.GRAY + ":");
            measurement = "Minutes";
            if(hours > 0) {
                runtimeStr.insert(0, ChatColor.WHITE + doubleDigit.format(hours) + ChatColor.GRAY + ":");
                measurement = "Hours";
            }
        }

        // Print the runtime
        sender.sendMessage(ChatColor.GOLD + "Runtime: " + ChatColor.WHITE + runtimeStr + " " + ChatColor.GRAY + measurement);
    }

    /**
     * Print the server status.
     *
     * @param sender The command sender to print the status to.
     */
    public void printServerStatus(CommandSender sender) {
        // Print the header
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Server Status:");

        // Print the server status
        sender.sendMessage(ChatColor.GOLD + "Detected Minecraft Version: " + ChatColor.WHITE + MinecraftUtils.getMinecraftVersion());
        sender.sendMessage(ChatColor.GOLD + "Detected Minecraft Server: " + ChatColor.WHITE + MinecraftUtils.getServerType().getName());
        sender.sendMessage(ChatColor.GOLD + "Server Version: " + ChatColor.WHITE + Bukkit.getVersion());
        sender.sendMessage(ChatColor.GOLD + "Bukkit Version: " + ChatColor.WHITE + Bukkit.getBukkitVersion());
        sender.sendMessage(ChatColor.GOLD + "Running Plugins: " + ChatColor.WHITE + Bukkit.getPluginManager().getPlugins().length);

        // Get the world manager
        sender.sendMessage(ChatColor.GOLD + "Loaded Worlds: " + ChatColor.WHITE + Bukkit.getWorlds().size());

        // Print the server time
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sender.sendMessage(ChatColor.GOLD + "Server Time: " + ChatColor.WHITE + dateFormat.format(new Date()));
    }

    /**
     * Print the machine status.
     *
     * @param sender The command sender to print the status to.
     */
    public void printMachineStatus(CommandSender sender) {
        // Print the header
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Machine Status:");

        // Return the machine status
        sender.sendMessage(ChatColor.GOLD + "OS Name: " + ChatColor.WHITE + System.getProperty("os.name"));
        sender.sendMessage(ChatColor.GOLD + "OS Architecture: " + ChatColor.WHITE + SystemUtils.getSystemArchNumber() + "-bit" + ChatColor.GRAY + " (" + SystemUtils.getSystemArchFull() + ")");
        sender.sendMessage(ChatColor.GOLD + "OS Version: " + ChatColor.WHITE + System.getProperty("os.version"));
        sender.sendMessage(ChatColor.GOLD + "Java Version: " + ChatColor.WHITE + SystemUtils.getJavaVersion() + ChatColor.GRAY + " (" + SystemUtils.getJavaArchValue() + "-bit)");
    }
}
