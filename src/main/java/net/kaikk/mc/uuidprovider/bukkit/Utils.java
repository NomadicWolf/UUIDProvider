package net.kaikk.mc.uuidprovider.bukkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.plugin.java.JavaPlugin;

public class Utils {
	public static void copyAsset(JavaPlugin instance, String assetName) {
		File file = new File(instance.getDataFolder(), assetName);
		file.getParentFile().mkdirs();
		if (!file.exists()) {
			try {
				Files.copy(getAsset(instance, assetName),
						file.getAbsoluteFile().toPath(),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static InputStream getAsset(JavaPlugin instance, String assetName) {
		return instance.getResource("assets/"+instance.getName().toLowerCase()+"/"+assetName);
	}
}
