package com.timvisee.glowstonelanterns.command.executable;

import com.timvisee.glowstonelanterns.GlowstoneLanterns;
import com.timvisee.glowstonelanterns.command.CommandParts;
import com.timvisee.glowstonelanterns.command.ExecutableCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends ExecutableCommand {

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

        // Check whether the creation mode is enabled, toggle the prebuilt lanterns mode if that's the case
        if(GlowstoneLanterns.instance.isGLEnabled((Player) sender))
            GlowstoneLanterns.instance.togglePlaceFinishedLanterns((Player) sender, "", true, false);

        // Toggle the creation mode
        GlowstoneLanterns.instance.toggleGL((Player) sender);

        // Return the result
        return true;
    }
}
