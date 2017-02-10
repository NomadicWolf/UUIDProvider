package net.kaikk.mc.uuidprovider.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import net.kaikk.mc.uuidprovider.UUIDProvider;

public class UUIDProviderBukkit extends JavaPlugin {
	public void onEnable() {
    	try {
			UUIDProvider.init(new ConfigBukkit(this));
			this.getLogger().info("Loaded "+UUIDProvider.getInstance().getCachedPlayersData().size()+" cached players");
		} catch (Exception e1) {
			this.getLogger().severe("A MySQL database is required! UUIDProvider won't work without a MySQL database!");
			throw new RuntimeException(e1);
		}
    	this.getCommand(this.getName()).setExecutor(new CommandExec(this));
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
	}
}
