package net.kaikk.mc.uuidprovider.sponge;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import net.kaikk.mc.uuidprovider.UUIDProvider;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id=PluginInfo.id, name = PluginInfo.name, version = PluginInfo.version, description = PluginInfo.description, dependencies = {@Dependency(id="kaiscommons")})
public class UUIDProviderSponge {
	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	@Inject
	private Logger logger;

	private UUIDProviderCache cache;

	@Listener
	public void onGamePreInit(GamePreInitializationEvent event) throws Exception {
		this.load();
		this.cache = new UUIDProviderCache();
		Sponge.getServer().getGameProfileManager().setCache(cache);
	}

	public void load() throws Exception {
		UUIDProvider.init(new ConfigSponge(this));
		this.logger().info("Loaded "+UUIDProvider.getInstance().getCachedPlayersData().size()+" cached players");
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) throws Exception {
		// Register command
		//Sponge.getCommandManager().register(this, CommandSpec.builder().description(Text.of("Unimplemented Commands")).arguments(GenericArguments.remainingJoinedStrings(Text.of("args"))).executor(new UnimplementedCommand()).build(), "uuidprovider");
	}

	@Listener
	public void onServerReload(GameReloadEvent event) throws Exception {
		this.load();
	}

	@Listener(beforeModifications = true, order=Order.PRE)
	public void onPlayerLogin(ClientConnectionEvent.Auth event) {
		// cache this player
		UUIDProvider.get(event.getProfile().getUniqueId());
	}

	public Logger logger() {
		return logger;
	}

	public ConfigurationLoader<CommentedConfigurationNode> getConfigManager() {
		return configManager;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public UUIDProviderCache getCache() {
		return cache;
	}
}
