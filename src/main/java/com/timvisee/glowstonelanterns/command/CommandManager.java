package com.timvisee.glowstonelanterns.command;

import com.timvisee.glowstonelanterns.command.executable.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CommandManager {

    /** The list of commandDescriptions. */
    private List<CommandDescription> commandDescriptions = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param registerCommands True to register the commands, false otherwise.
     */
    public CommandManager(boolean registerCommands) {
        // Register the commands
        if(registerCommands)
            registerCommands();
    }

    /**
     * Register all commands.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public void registerCommands() {
        // Register the base Glowstone Lanterns command
        CommandDescription glowstoneLanternsCommand = new CommandDescription(
                new GlowstoneLanternsCommand(),
                new ArrayList<String>() {{
                    add("glowstonelanterns");
                    add("glowstonelantern");
                    add("gl");
                }},
                "Main command",
                "The main Glowstone Lanterns command. The root for all the other commands.", null);

        // Register the create command
        CommandDescription createCommand = new CommandDescription(
                new CreateCommand(),
                new ArrayList<String>() {{
                    add("create");
                    add("c");
                    add("build");
                    add("make");
                    add("place");
                }},
                "Create Glowstone Lanterns",
                "Toggle the creation mode to place down glowstone lanterns",
                glowstoneLanternsCommand);
        createCommand.setCommandPermissions("glowstonelanterns.command.create", CommandPermissions.DefaultPermission.OP_ONLY);

        // Register the help command
        CommandDescription helpCommand = new CommandDescription(
                new HelpCommand(),
                new ArrayList<String>() {{
                    add("help");
                    add("hlp");
                    add("h");
                    add("sos");
                    add("?");
                }},
                "View help",
                "View detailed help pages about Glowstone Lanterns commands.",
                glowstoneLanternsCommand);
        helpCommand.addArgument(new CommandArgumentDescription("query", "The command or query to view help for.", true));
        helpCommand.setMaximumArguments(false);

        // Register the reload command
        CommandDescription reloadCommand = new CommandDescription(
                new ReloadCommand(),
                new ArrayList<String>() {{
                    add("reload");
                    add("rld");
                    add("r");
                }},
                "Reload Glowstone Lanterns",
                "Reload the Glowstone Lanterns plugin and it's files.",
                glowstoneLanternsCommand);
        reloadCommand.setCommandPermissions("glowstonelanterns.command.reload", CommandPermissions.DefaultPermission.OP_ONLY);
        reloadCommand.addArgument(new CommandArgumentDescription("force", "True or False to force reload.", true));

        // Register the reload permissions command
        CommandDescription reloadPermissionsCommand = new CommandDescription(
                new ReloadPermissionsCommand(),
                new ArrayList<String>() {{
                    add("reloadpermissions");
                    add("reloadpermission");
                    add("reloadperms");
                    add("rp");
                }},
                "Reload permissions",
                "Reload the permissions system and rehook the installed permissions system.",
                glowstoneLanternsCommand);
        reloadPermissionsCommand.setCommandPermissions("glowstonelanterns.command.reloadpermissions", CommandPermissions.DefaultPermission.OP_ONLY);

        // Register the status command
        CommandDescription statusCommand = new CommandDescription(
                new StatusCommand(),
                new ArrayList<String>() {{
                    add("status");
                    add("stats");
                    add("s");
                }},
                "Status info",
                "Show detailed plugin status.",
                glowstoneLanternsCommand);
        statusCommand.setMaximumArguments(false);
        statusCommand.setCommandPermissions("glowstonelanterns.command.status", CommandPermissions.DefaultPermission.OP_ONLY);

        // Register the version command
        CommandDescription versionCommand = new CommandDescription(
                new VersionCommand(),
                new ArrayList<String>() {{
                    add("version");
                    add("ver");
                    add("v");
                    add("about");
                    add("info");
                }},
                "Version info",
                "Show detailed information about the installed Glowstone Lanterns version, and shows the developers, contributors, license and other information.",
                glowstoneLanternsCommand);
        versionCommand.setMaximumArguments(false);

        // Add the base command to the commands array
        this.commandDescriptions.add(glowstoneLanternsCommand);
    }

    /**
     * Get the list of command descriptions
     *
     * @return List of command descriptions.
     */
    public List<CommandDescription> getCommandDescriptions() {
        return this.commandDescriptions;
    }

    /**
     * Get the number of command description count.
     *
     * @return Command description count.
     */
    public int getCommandDescriptionCount() {
        return this.getCommandDescriptions().size();
    }

    /**
     * Find the best suitable command for the specified reference.
     *
     * @param queryReference The query reference to find a command for.
     *
     * @return The command found, or null.
     */
    public FoundCommandResult findCommand(CommandParts queryReference) {
        // Make sure the command reference is valid
        if(queryReference.getCount() <= 0)
            return null;

        // Get the base command description
        for(CommandDescription commandDescription : this.commandDescriptions) {
            // Check whether there's a command description available for the current command
            if(!commandDescription.isSuitableLabel(queryReference))
                continue;

            // Find the command reference, return the result
            return commandDescription.findCommand(queryReference);
        }

        // No applicable command description found, return false
        return null;
    }
}