package com.timvisee.glowstonelanterns.command.executable;

import com.sun.scenario.effect.Glow;
import com.timvisee.glowstonelanterns.GlowstoneLanterns;
import com.timvisee.glowstonelanterns.command.CommandParts;
import com.timvisee.glowstonelanterns.command.ExecutableCommand;
import com.timvisee.glowstonelanterns.util.Profiler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SaveCommand extends ExecutableCommand {

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
        // Profile the reload process
        Profiler p = new Profiler(true);

        // Save the lanterns and show the result
        if (GlowstoneLanterns.instance.saveLanterns())
            sender.sendMessage(ChatColor.GREEN + "Successfully saved " + GlowstoneLanterns.instance.countLanterns() + " lanterns, took " + p.getTimeFormatted() + "!");
        else
            sender.sendMessage(ChatColor.DARK_RED + "Failed to save glowstone lanterns!");

        // Return the result
        return true;
    }
}
