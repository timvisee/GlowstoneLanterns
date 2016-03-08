package com.timvisee.glowstonelanterns.command.executable;

import com.timvisee.glowstonelanterns.GlowstoneLanterns;
import com.timvisee.glowstonelanterns.command.CommandParts;
import com.timvisee.glowstonelanterns.command.ExecutableCommand;
import com.timvisee.glowstonelanterns.permission.PermissionsManager;
import com.timvisee.glowstonelanterns.util.Profiler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class ReloadPermissionsCommand extends ExecutableCommand {

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
        // Profile the permissions reload process
        Profiler p = new Profiler(true);

        // Get the logger instance
        Logger log = GlowstoneLanterns.instance.getLogger();

        // Show a status message
        sender.sendMessage(ChatColor.YELLOW + "Reloading permissions...");
        log.info("Reloading permissions...");

        // Get the permissions manager and make sure it's valid
        PermissionsManager permissionsManager = GlowstoneLanterns.instance.getPermissionsManager();
        if(permissionsManager == null) {
            log.info("Failed to access the permissions manager after " + p.getTimeFormatted() + "!");
            sender.sendMessage(ChatColor.DARK_RED + "Failed to access the permissions manager after " + p.getTimeFormatted() + "!");
            return true;
        }

        // Reload the permissions service, show an error on failure
        if(!permissionsManager.reload()) {
            log.info("Failed to reload permissions after " + p.getTimeFormatted() + "!");
            sender.sendMessage(ChatColor.DARK_RED + "Failed to reload permissions after " + p.getTimeFormatted() + "!");
            return true;
        }

        // Show a success message
        log.info("Permissions reloaded successfully, took " + p.getTimeFormatted() + "!");
        sender.sendMessage(ChatColor.GREEN + "Permissions reloaded successfully, took " + p.getTimeFormatted() + "!");

        // Get the used permissions system

        // Get and show the permissions system being used
        String permissionsSystem = ChatColor.RED + "None";
        if(permissionsManager.isHooked())
            permissionsSystem = ChatColor.GOLD + permissionsManager.getUsedPermissionsSystemType().getName();
        log.info("Used permissions system: " + permissionsSystem);
        sender.sendMessage(ChatColor.GREEN + "Used permissions system: " + permissionsSystem);
        return true;
    }
}
