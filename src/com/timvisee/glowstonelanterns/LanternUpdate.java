package com.timvisee.glowstonelanterns;

import org.bukkit.Server;

public class LanternUpdate {
	
	Lantern l;
	int newState = 0;
	
	LanternUpdate(Lantern l, int newState) {
		this.l = l;
		this.newState = newState;
	}
	
	public void updateLantern(Server s) {
		l.setState(s, newState);
	}
}