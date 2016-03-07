package com.timvisee.glowstonelanterns;

import org.bukkit.Server;

public class LanternUpdate {
	
	Lantern l;
	LanternState newState = LanternState.UNKNOWN;
	
	LanternUpdate(Lantern l, LanternState newState) {
		this.l = l;
		this.newState = newState;
	}
	
	public void updateLantern(Server s) {
		l.setState(s, newState);
	}
}
