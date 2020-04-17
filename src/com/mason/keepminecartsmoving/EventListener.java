package com.mason.keepminecartsmoving;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class EventListener implements Listener {
	// a set of all minecarts (gets cleared every tick)
	private Set<Minecart> minecarts = new HashSet<>();
	// the previous set of all minecarts
	private Set<Minecart> minecartsOld = new HashSet<>();
	// any destroyed minecarts that need to be cleaned up
	private List<Minecart> destroyedMinecarts = new ArrayList<>();
	
	public static final double MINECART_SPEED_THRESHOLD = 0.05D;
	
	public EventListener(KeepMinecartsMoving plugin) {
		// run this every tick
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				// combine the two (it's inside a thread, so minecarts may not be full when this runs, so it combines with the previous)
				minecartsOld.addAll(minecarts);
				
				// if a minecart got destroyed, it should turn off forceLoaded where it was
				for(Minecart m : destroyedMinecarts) {
					// this will make sure th
					minecartsOld.add(m);
					
					Location minecartLoc = m.getLocation();
					World w = minecartLoc.getWorld();
					
					Chunk minecartChunk = minecartLoc.getChunk();
					
					int minecartChunkX = minecartChunk.getX();
					int minecartChunkZ = minecartChunk.getZ();
					
					Chunk minecartChunkNorth = w.getChunkAt(minecartChunkX, minecartChunkZ - 1);
					Chunk minecartChunkEast = w.getChunkAt(minecartChunkX + 1, minecartChunkZ);
					Chunk minecartChunkSouth = w.getChunkAt(minecartChunkX, minecartChunkZ + 1);
					Chunk minecartChunkWest = w.getChunkAt(minecartChunkX - 1, minecartChunkZ);
					
					minecartChunkNorth.setForceLoaded(false);
					minecartChunkEast.setForceLoaded(false);
					minecartChunkSouth.setForceLoaded(false);
					minecartChunkWest.setForceLoaded(false);
				}
				
				// set force loaded to false all around the minecarts, so if one moved out of the chunk, it'll clean up around itself
				// resolves any issues with overlapping carts
				for(Minecart m : minecartsOld) {
					Location minecartLoc = m.getLocation();
					World w = minecartLoc.getWorld();
					
					Chunk minecartChunk = minecartLoc.getChunk();
					
					int minecartChunkX = minecartChunk.getX();
					int minecartChunkZ = minecartChunk.getZ();
					
					Chunk minecartChunkNorth = w.getChunkAt(minecartChunkX, minecartChunkZ - 2);
					Chunk minecartChunkNorthEast = w.getChunkAt(minecartChunkX + 1, minecartChunkZ - 1);
					Chunk minecartChunkEast = w.getChunkAt(minecartChunkX + 2, minecartChunkZ);
					Chunk minecartChunkSouthEast = w.getChunkAt(minecartChunkX + 1, minecartChunkZ + 1);
					Chunk minecartChunkSouth = w.getChunkAt(minecartChunkX, minecartChunkZ + 2);
					Chunk minecartChunkSouthWest = w.getChunkAt(minecartChunkX - 1, minecartChunkZ + 1);
					Chunk minecartChunkWest = w.getChunkAt(minecartChunkX - 2, minecartChunkZ);
					Chunk minecartChunkNorthWest = w.getChunkAt(minecartChunkX - 1, minecartChunkZ - 1);
					
					minecartChunkNorth.setForceLoaded(false);
					minecartChunkNorthEast.setForceLoaded(false);
					minecartChunkEast.setForceLoaded(false);
					minecartChunkSouthEast.setForceLoaded(false);
					minecartChunkSouth.setForceLoaded(false);
					minecartChunkSouthWest.setForceLoaded(false);
					minecartChunkWest.setForceLoaded(false);
					minecartChunkNorthWest.setForceLoaded(false);
				}
				
				// turn all the chunks back on for the minecarts that still exist
				for(Minecart m : minecartsOld) {
					// don't turn it back on if it's not moving fast enough or dead
					Vector vel = m.getVelocity();
					
					if(m.isDead() || (Math.abs(vel.getX()) <= MINECART_SPEED_THRESHOLD &&
							   		  Math.abs(vel.getY()) <= MINECART_SPEED_THRESHOLD &&
							   		  Math.abs(vel.getZ()) <= MINECART_SPEED_THRESHOLD)) {
						// don't do anything if it's moving too slow or dead, the game will clean up
						// non force loaded chunks
					} else {
						Location minecartLoc = m.getLocation();
						World w = minecartLoc.getWorld();
						
						Chunk minecartChunk = minecartLoc.getChunk();
						
						int minecartChunkX = minecartChunk.getX();
						int minecartChunkZ = minecartChunk.getZ();
						
						Chunk minecartChunkNorth = w.getChunkAt(minecartChunkX, minecartChunkZ - 1);
						Chunk minecartChunkEast = w.getChunkAt(minecartChunkX + 1, minecartChunkZ);
						Chunk minecartChunkSouth = w.getChunkAt(minecartChunkX, minecartChunkZ + 1);
						Chunk minecartChunkWest = w.getChunkAt(minecartChunkX - 1, minecartChunkZ);
						
						minecartChunkNorth.setForceLoaded(true);
						minecartChunkEast.setForceLoaded(true);
						minecartChunkSouth.setForceLoaded(true);
						minecartChunkWest.setForceLoaded(true);
					}
				}
				
				minecartsOld.clear();
				
				// swap minecarts and minecartsOld (I think this makes it faster, no clue :)
				Set<Minecart> tmp = minecartsOld;
				minecartsOld = minecarts;
				minecarts = tmp;
			}
		}, 1l, 1l);
	}
	
	// this will load a chunk if it isn't already, and make it force loaded
	private void checkAndLoadChunk(Chunk c) {
		if(!c.isLoaded()) {
			c.load();
		}
		
		if(!c.isForceLoaded()) {
			c.setForceLoaded(true);
		}
	}
	
	@EventHandler
	public void vehicleMove(VehicleMoveEvent e) {
		Vehicle v = e.getVehicle();
		
		// if it's a minecart
		if(v instanceof Minecart) {
			Minecart m = (Minecart) v;
			
			Vector vel = m.getVelocity();
			
			// and it's moving above the threshold speed
			if(Math.abs(vel.getX()) > MINECART_SPEED_THRESHOLD ||
			   Math.abs(vel.getY()) > MINECART_SPEED_THRESHOLD ||
			   Math.abs(vel.getZ()) > MINECART_SPEED_THRESHOLD) {		
				Location minecartLoc = v.getLocation();
				World w = minecartLoc.getWorld();
				
				Chunk minecartChunk = minecartLoc.getChunk();
				
				int minecartChunkX = minecartChunk.getX();
				int minecartChunkZ = minecartChunk.getZ();
				
				// check and load chunks around it
				Chunk minecartChunkNorth = w.getChunkAt(minecartChunkX, minecartChunkZ - 1);
				Chunk minecartChunkEast = w.getChunkAt(minecartChunkX + 1, minecartChunkZ);
				Chunk minecartChunkSouth = w.getChunkAt(minecartChunkX, minecartChunkZ + 1);
				Chunk minecartChunkWest = w.getChunkAt(minecartChunkX - 1, minecartChunkZ);
				
				checkAndLoadChunk(minecartChunk);
				checkAndLoadChunk(minecartChunkNorth);
				checkAndLoadChunk(minecartChunkEast);
				checkAndLoadChunk(minecartChunkSouth);
				checkAndLoadChunk(minecartChunkWest);
				
				minecarts.add(m);
			}
		}
	}
	
	// in order to clean up around a destroyed minecart
	@EventHandler
	public void minecartDestroyed(VehicleDestroyEvent e) {
		Vehicle v = e.getVehicle();
		
		if(v instanceof Minecart) {
			destroyedMinecarts.add((Minecart) v);
		}
	}
}