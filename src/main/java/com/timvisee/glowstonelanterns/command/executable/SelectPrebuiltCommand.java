package com.timvisee.glowstonelanterns.command.executable;

import com.timvisee.glowstonelanterns.GlowstoneLanterns;
import com.timvisee.glowstonelanterns.command.CommandParts;
import com.timvisee.glowstonelanterns.command.ExecutableCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SelectPrebuiltCommand extends ExecutableCommand {

    /**
     * Execute the command.
     *
     * @param sender The command sender.
     * @param commandReference The command reference.
     * @param commandArguments The command arguments.
     *
     * @return True if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean executeCommand(CommandSender sender, CommandParts commandReference, CommandParts commandArguments) {
        // Make sure the command is executed by an in-game player
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "You need to be in-game to use this command!");
            return true;
        }

        // Get the player sender
        Player player = (Player) sender;

        // Check whether a lantern name is given
        if(commandArguments.getCount() > 0) {
            // Get the name of the lantern
            String lanternName = commandArguments.get(0);

            // Deselect the prebuilt lantern if no name is entered
            if(lanternName.trim().length() == 0) {
                GlowstoneLanterns.instance.togglePlaceFinishedLanterns(player, "", true, true);
                player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/" + commandReference.toString() + ChatColor.ITALIC + " <name>" + ChatColor.YELLOW + " to select a prebuilt lantern.");
                player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/" + commandReference.get(0) + " list" + ChatColor.YELLOW + " to list all prebuilt lanterns.");
                return true;
            }

            // Enable the lantern creation mode if it isn't currently enabled
            if(!GlowstoneLanterns.instance.isGLEnabled(player))
                GlowstoneLanterns.instance.toggleGL(player);

            // Select the prebuilt lantern
            // TODO: Should we make sure the lantern exists?
            GlowstoneLanterns.instance.togglePlaceFinishedLanterns(player, lanternName, false, true);

            // Show a status message
            player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/" + commandReference.toString() + ChatColor.YELLOW + " without a name to disable this mode.");

        } else {
            // Deselect the prebuilt lantern
            GlowstoneLanterns.instance.togglePlaceFinishedLanterns(player, "", true, true);
            player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/" + commandReference.toString() + ChatColor.ITALIC + " <name>" + ChatColor.YELLOW + " to select a prebuilt lantern.");
            player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.WHITE + "/" + commandReference.get(0) + " list" + ChatColor.YELLOW + " to list all prebuilt lanterns.");
        }

        // Return the result
        return true;
    }
}
