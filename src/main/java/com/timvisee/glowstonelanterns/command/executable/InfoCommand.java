package com.timvisee.glowstonelanterns.command.executable;

import com.timvisee.glowstonelanterns.GlowstoneLanterns;
import com.timvisee.glowstonelanterns.command.CommandParts;
import com.timvisee.glowstonelanterns.command.ExecutableCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends ExecutableCommand {

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

        // Show the creation mode
        if(GlowstoneLanterns.instance.isGLEnabled((Player) sender))
            sender.sendMessage(ChatColor.YELLOW + "Glowstone Lanterns creation mode: " + ChatColor.GREEN + "Enabled");
        else
            sender.sendMessage(ChatColor.YELLOW + "Glowstone Lanterns creation mode: " + ChatColor.DARK_RED + "Disabled");

        // Show whether a prebuilt lantern is selected or not
        if(GlowstoneLanterns.instance.isGLPrebuildEnabled((Player) sender))
            sender.sendMessage(ChatColor.YELLOW + "Glowstone Lanterns selected prebuilt lantern: " + ChatColor.GREEN + "Yes");
        else
            sender.sendMessage(ChatColor.YELLOW + "Glowstone Lanterns selected prebuilt lantern: " + ChatColor.DARK_RED + "No");

        // Return the result
        return true;
    }
}
