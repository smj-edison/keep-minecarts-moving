package com.mason.keepminecartsmoving;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KeepMinecartsMoving extends JavaPlugin {
	public static final String VERSION = "v0.1.1";
	
	PluginManager pm;
	EventListener listener;
	
	@Override
	public void onEnable() {
		pm = getServer().getPluginManager();
		
		listener = new EventListener(this);
		pm.registerEvents(listener, this);
		
		getLogger().info("Enabled KeepMinecartsMoving (" + VERSION + ")");
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Disabled KeepMinecartsMoving (" + VERSION + ")");
	}
}
