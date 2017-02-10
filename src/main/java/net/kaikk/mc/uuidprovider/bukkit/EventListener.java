package net.kaikk.mc.uuidprovider.bukkit;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import net.kaikk.mc.uuidprovider.UUIDProvider;

public class EventListener implements Listener {
	private UUIDProviderBukkit instance;

	EventListener(UUIDProviderBukkit instance) {
		this.instance = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
		if (UUIDProvider.getInstance().getConfig().dummyMode) {
			UUIDProvider.cacheInternal(event.getUniqueId(), event.getName());
			return;
		}

		// cache this player - we can't trust Bukkit's UUID: it may give a random uuid if the server is in offline mode or Mojang servers are down!
		UUID uuid = UUIDProvider.get(event.getName());
		if (uuid==null) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Invalid UUID or Mojang's servers down. Try later.");
			instance.getLogger().warning("UUIDProvider couldn't retrieve UUID for "+event.getName());
		}
	}
}
