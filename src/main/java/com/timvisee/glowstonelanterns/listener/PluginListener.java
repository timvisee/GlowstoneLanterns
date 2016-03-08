package com.timvisee.glowstonelanterns.listener;

import com.timvisee.glowstonelanterns.GlowstoneLanterns;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;

public class PluginListener implements Listener {

    /**
     * Called when a plugin is enabled.
     *
     * @param event Event reference.
     */
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        // Call the onPluginEnable method in the permissions manager
        GlowstoneLanterns.instance.getPermissionsManager().onPluginEnable(event);
    }

    /**
     * Called when a plugin is disabled.
     *
     * @param event Event reference.
     */
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        // Call the onPluginDisable method in the permissions manager
        GlowstoneLanterns.instance.getPermissionsManager().onPluginDisable(event);
    }
}
