package com.timvisee.glowstonelanterns.lantern;

public enum LanternState {

	UNKNOWN(0, "UNKNOWN"),
	DAY(1, "DAY"),
	NIGHT(2, "NIGHT"),
	RAIN(3, "RAIN");
	
	private int state;
	private String name;
	
	/**
	 * Constructor
	 * @param state Lantern State
	 * @param name State name
	 */
	LanternState(int state, String name) {
		this.state = state;
	}
	
	/**
	 * Get the lantern state ID
	 * @return Lantern State ID
	 */
	public int getId() {
		return this.state;
	}
	
	/**
	 * Get the lantern state name
	 * @return Lantern state name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the lantern state by it's ID. This method is performance intensive.
	 * @param i Lantern State ID
	 * @return Lantern State
	 */
	public static LanternState get(int i) {
		for(LanternState s : values())
			if(s.getId() == i)
				return s;
		return LanternState.UNKNOWN;
	}

	/**
	 * Get the lantern state by a state name. This method is performance intensive.
	 * @param name Lantern state name
	 * @return Lantern State
	 */
	public static LanternState getByName(String name) {
		for(LanternState s : values())
			if(s.getName().equalsIgnoreCase(name.trim()))
				return s;
		return LanternState.UNKNOWN;
	}
}
