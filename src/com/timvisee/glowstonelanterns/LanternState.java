package com.timvisee.glowstonelanterns;

public enum LanternState {

	UNKNOWN(0),
	DAY(1),
	NIGHT(2),
	RAIN(3);
	
	private int state;
	
	/**
	 * Constructor
	 * @param state Lantern State
	 */
	LanternState(int state) {
		this.state = state;
	}
	
	/**
	 * Get the lantern state ID
	 * @return Lantern State ID
	 */
	public int getStateId() {
		return this.state;
	}

	/**
	 * Get the lantern state by it's ID
	 * @param i Lantern State ID
	 * @return Lantern State
	 */
	public static LanternState get(int i) {
		for(LanternState s : values())
			if(s.getStateId() == i)
				return s;
		return LanternState.UNKNOWN;
	}
}
