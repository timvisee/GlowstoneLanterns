package com.timvisee.glowstonelanterns;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Lantern {

	String w = "world";
	int x, y, z = 0;
	
	/**
	 * lanternState is current state of the lantern
	 * lanternState = 0 = unknown
	 * lanternState = 1 = day
	 * lanternState = 2 = night
	 * lanternState = 3 = rain
	 */
	private LanternState lanternState = LanternState.UNKNOWN;
	
	private int lanternDayType = 0;
	private byte lanternDayData;
	private int lanternNightType = 0;
	private byte lanternNightData;
	private int lanternRainType = 0;
	private byte lanternRainData;
	
	
	/**
	 * Define a lantern
	 * @param world The world of the lantern
	 * @param x The X coordinate of the lantern
	 * @param y The Y coordinate of the lantern
	 * @param z The Z coordinate of the lantern
	 * @param lanternState The lantern state
	 * @param lanternDayType The lantern day type
	 * @param lanternDayData The lantern day data
	 * @param lanternNightType The lantern night type
	 * @param lanternNightData The lantern night data
	 * @param lanternRainType The lantern rain type
	 * @param lanternRainData The lantern rain data
	 */
	public Lantern(
			String world, int x, int y, int z,
			LanternState lanternState, 
			int lanternDayType, byte lanternDayData,
			int lanternNightType, byte lanternNightData,
			int lanternRainType, byte lanternRainData) {
		setWorld(world); //world
		setBlockX(x); //x
		setBlockY(y); //y
		setBlockZ(z); //z
		setVariableState(lanternState); //state
		setDayType(lanternDayType); //day type
		setDayData(lanternDayData); //day data
		setNightType(lanternNightType); //night type
		setNightData(lanternNightData); //night data
		setRainType(lanternRainType); //rain type
		setRainData(lanternRainData); //rain data
	}
	
	/**
	 * Define a glowsone lantern using a data string
	 * @param dataString The data string
	 */
	public Lantern(String dataString) {
		List<String> data = Arrays.asList(dataString.split(";"));
		
		setWorld(data.get(0)); //world
		setBlockX(Integer.parseInt(data.get(1))); //x
		setBlockY(Integer.parseInt(data.get(2))); //y
		setBlockZ(Integer.parseInt(data.get(3))); //z
		setVariableState(LanternState.get(Integer.parseInt(data.get(4)))); //state
		setDayType(Integer.parseInt(data.get(5))); //day type
		setDayData(Byte.parseByte(data.get(6))); //day data
		setNightType(Integer.parseInt(data.get(7))); //night type
		setNightData(Byte.parseByte(data.get(8))); //night data
		setRainType(Integer.parseInt(data.get(9))); //rain type
		setRainData(Byte.parseByte(data.get(10))); //rain data
	}
	
	/**
	 * This will return the data string of this lantern to save the lantern to a file
	 * @return Returns the data string
	 */
	public String getDataString() {
		String dataString = "";
		
		dataString = dataString + getWorld(); //world
		dataString = dataString + ";" + String.valueOf(getBlockX()); //x
		dataString = dataString + ";" + String.valueOf(getBlockY()); //y
		dataString = dataString + ";" + String.valueOf(getBlockZ()); //z
		dataString = dataString + ";" + String.valueOf(getState().getId()); // state
		dataString = dataString + ";" + String.valueOf(getDayType()); //day type
		dataString = dataString + ";" + String.valueOf(getDayData()); //day data
		dataString = dataString + ";" + String.valueOf(getNightType()); //night type
		dataString = dataString + ";" + String.valueOf(getNightData()); //night data
		dataString = dataString + ";" + String.valueOf(getRainType()); //rain type
		dataString = dataString + ";" + String.valueOf(getRainData()); //rain data
		
		return dataString;
	}
	
	
	
	// Getters and Setters
	public String getWorld() {
		return w;
	}
	
	public void setWorld(World world) {
		setWorld(world.getName());
	}
	
	private void setWorld(String world) {
		this.w = world;
	}
	
	public int getBlockX() {
		return x;
	}
	
	private void setBlockX(int x) {
		this.x = x;
	}
	
	public int getBlockY() {
		return y;
	}
	
	private void setBlockY(int y) {
		this.y = y;
	}
	
	public int getBlockZ() {
		return z;
	}
	
	private void setBlockZ(int z) {
		this.z = z;
	}
	
	public Block getBlock(Server server) {
		return server.getWorld(getWorld()).getBlockAt(
				getBlockX(),
				getBlockY(),
				getBlockZ());
	}
	
	public int getDayType() {
		return lanternDayType;
	}
	
	public void setDayType(int typeId) {
		lanternDayType = typeId;
	}
	
	public byte getDayData() {
		return lanternDayData;
	}
	
	public void setDayData(byte data) {
		lanternDayData = data;
	}
	
	public int getNightType() {
		return lanternNightType;
	}
	
	public void setNightType(int typeId) {
		lanternNightType = typeId;
	}
	
	public byte getNightData() {
		return lanternNightData;
	}
	
	public void setNightData(byte data) {
		lanternNightData = data;
	}
	
	public int getRainType() {
		return lanternRainType;
	}
	
	public void setRainType(int typeId) {
		lanternRainType = typeId;
	}
	
	public byte getRainData() {
		return lanternRainData;
	}
	
	public void setRainData(byte data) {
		lanternRainData = data;
	}
	
	/**
	 * This returns the current lantern state
	 * @return Current Lantern State
	 */
	public LanternState getState() {
		return lanternState;
	}
	
	/**
	 * Set the current lantern state,
	 * @param s Lantern State
	 * @return Returns false when the lantern state isn't valid, if the lantern block isn't changed.
	 * Returns true when the lantern state and the lantern block is succesfully changed.
	 */
	public boolean setState(Server server, LanternState s) {
		if(s == LanternState.UNKNOWN || s == LanternState.DAY) {
			changeLanternBlock(server, getDayType(), getDayData());
			lanternState = s;
			return true;
		}
		if(s == LanternState.NIGHT) {
			changeLanternBlock(server, getNightType(), getNightData());
			lanternState = s;
			return true;
		}
		if(s == LanternState.RAIN) {
			changeLanternBlock(server, getRainType(), getRainData());
			lanternState = s;
			return true;
		}
		return false;
	}
	
	/**
	 * This will set the lantern state of the lantern without changing the lantern block in the world
	 * @param server The server
	 * @param state The new state
	 */
	private void setVariableState(LanternState state) {
		this.lanternState = state;
	}
	
	/**
	 * This will change the block of the lantern in the world to any block you want
	 * @param server The server
	 * @param typeId The ID of the block
	 * @param data The data of the block
	 */
	private void changeLanternBlock(Server server, int typeId, byte data) {
		World world = server.getWorld(getWorld());
		Block block = world.getBlockAt(getBlockX(), getBlockY(), getBlockZ());
		
		block.setTypeId(typeId);
		block.setData(data);
		
		if(block.getType().equals(Material.REDSTONE_LAMP_ON)) {
			//net.minecraft.server.Block.byId[block.getTypeId()].interact(((CraftWorld)block.getWorld()).getHandle(),block.getX(), block.getY(), block.getZ(),((CraftPlayer)server.getPlayer("")).getHandle());
		
			//net.minecraft.server.Block.byId[block.getTypeId()].interact((net.minecraft.server.World) block.getWorld(), block.getX(), block.getY(), block.getZ(), (EntityHuman) server.getPlayer(""));
		
			//BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, 0, 1);
			//server.getPluginManager().callEvent(eventRedstone);
		}
	}
	
	/**
	 * This will tell you if the chunk where the lantern is on is loaded
	 * @param server The server
	 * @return Returns true when the chunk where the lantern is on is loaded
	 */
	public boolean isLanternChunkLoaded(Server server) {
		World world = server.getWorld(getWorld());
		Block lanternBlock = world.getBlockAt(getBlockX(), getBlockY(), getBlockZ());
		return lanternBlock.getChunk().isLoaded();
	}
}