package com.timvisee.glowstonelanterns.command.executable;

import com.timvisee.glowstonelanterns.GlowstoneLanterns;
import com.timvisee.glowstonelanterns.command.CommandParts;
import com.timvisee.glowstonelanterns.command.ExecutableCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ListCommand extends ExecutableCommand {

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
        // Get the list of prebuilt lanterns
        List<String> prebuiltLanternsList = GlowstoneLanterns.instance.prebuiltLanternsList();

        // Print the header
        sender.sendMessage(ChatColor.GOLD + "==========[ " + GlowstoneLanterns.getPluginName().toUpperCase() + " PREBUILT LANTERNS ]==========");
        sender.sendMessage(ChatColor.GOLD + " Prebuilt lanterns:");

        // Show the prebuilt lanterns
        for(String aPrebuiltLanternsList : prebuiltLanternsList)
            sender.sendMessage(ChatColor.WHITE + " " + aPrebuiltLanternsList.replaceAll(".gllantern", "") + ChatColor.DARK_GRAY + ".gllantern");

        // Show a special message if no prebuilt lanterns are available
        if(prebuiltLanternsList.size() == 0)
            sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + " No prebuilt lanterns available!");

        // Return the result
        return true;
    }
}
