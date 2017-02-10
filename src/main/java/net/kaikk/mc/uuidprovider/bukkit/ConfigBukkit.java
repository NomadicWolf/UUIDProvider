package net.kaikk.mc.uuidprovider.bukkit;

import javax.sql.DataSource;

import org.bukkit.plugin.java.JavaPlugin;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import net.kaikk.mc.uuidprovider.Config;

public class ConfigBukkit extends Config {
	public ConfigBukkit(JavaPlugin instance) {
		Utils.copyAsset(instance, "config.yml");
		instance.reloadConfig();

		this.dbHostname=instance.getConfig().getString("MySQL.Hostname");
		this.dbUsername=instance.getConfig().getString("MySQL.Username");
		this.dbPassword=instance.getConfig().getString("MySQL.Password");
		this.dbDatabase=instance.getConfig().getString("MySQL.Database");

		this.offlineMode=instance.getConfig().getBoolean("OfflineMode");
		this.dummyMode=instance.getConfig().getBoolean("DummyMode");
	}

	public DataSource getDataSource() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch(Exception e) {
			throw new RuntimeException("ERROR: Unable to load Java's MySQL database driver. Check to make sure you've installed it properly.");
		}

		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setURL("jdbc:mysql://"+dbHostname+"/"+dbDatabase);
		dataSource.setUser(dbUsername);
		dataSource.setPassword(dbPassword);
		dataSource.setDatabaseName(dbDatabase);
		return dataSource;
	}
}
