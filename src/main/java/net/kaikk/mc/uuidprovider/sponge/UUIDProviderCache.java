package net.kaikk.mc.uuidprovider.sponge;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;

import net.kaikk.mc.uuidprovider.PlayerData;
import net.kaikk.mc.uuidprovider.UUIDProvider;
import net.kaikk.mc.uuidprovider.UUIDProvider.Mode;

public class UUIDProviderCache implements GameProfileCache {
	private Map<UUID,GameProfile> cache = new ConcurrentHashMap<UUID,GameProfile>();

	@Override
	public boolean add(GameProfile profile, boolean overwrite, Date expiry) {
		if (!overwrite && UUIDProvider.get(profile.getUniqueId(), Mode.INTERNAL | Mode.DATABASE) != null) {
			return false;
		}
		try {
			UUIDProvider.cache(profile.getUniqueId(), profile.getName().get());
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean remove(GameProfile profile) {
		try {
			UUIDProvider.removeData(profile.getUniqueId());
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Collection<GameProfile> remove(Iterable<GameProfile> profiles) {
		for (GameProfile profile : profiles) {
			this.remove(profile);
		}
		return Collections.emptyList();
	}

	@Override
	public void clear() {
		try {
			UUIDProvider.clearCache();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Optional<GameProfile> getById(UUID uniqueId) {
		return this.get(uniqueId, Mode.INTERNAL | Mode.DATABASE);
	}

	@Override
	public Map<UUID, Optional<GameProfile>> getByIds(Iterable<UUID> uniqueIds) {
		final Map<UUID,Optional<GameProfile>> map = new HashMap<UUID,Optional<GameProfile>>();
		for (UUID uuid : uniqueIds) {
			map.put(uuid, this.getById(uuid));
		}

		return map;
	}

	@Override
	public Optional<GameProfile> lookupById(UUID uniqueId) {
		return this.get(uniqueId, Mode.MOJANG);
	}

	@Override
	public Map<UUID, Optional<GameProfile>> lookupByIds(Iterable<UUID> uniqueIds) {
		final Map<UUID,Optional<GameProfile>> map = new HashMap<UUID,Optional<GameProfile>>();
		for (UUID uuid : uniqueIds) {
			map.put(uuid, this.lookupById(uuid));
		}

		return map;
	}

	@Override
	public Optional<GameProfile> getOrLookupById(UUID uniqueId) {
		return this.get(uniqueId, Mode.ALL);
	}

	@Override
	public Map<UUID, Optional<GameProfile>> getOrLookupByIds(Iterable<UUID> uniqueIds) {
		final Map<UUID,Optional<GameProfile>> map = new HashMap<UUID,Optional<GameProfile>>();
		for (UUID uuid : uniqueIds) {
			map.put(uuid, this.getOrLookupById(uuid));
		}

		return map;
	}

	@Override
	public Optional<GameProfile> getByName(String name) {
		return this.get(name, Mode.INTERNAL | Mode.DATABASE);
	}

	@Override
	public Map<String, Optional<GameProfile>> getByNames(Iterable<String> names) {
		final Map<String,Optional<GameProfile>> map = new HashMap<String,Optional<GameProfile>>();
		for (String name : names) {
			map.put(name, this.getByName(name));
		}

		return map;
	}

	@Override
	public Optional<GameProfile> lookupByName(String name) {
		return this.get(name, Mode.MOJANG);
	}

	@Override
	public Map<String, Optional<GameProfile>> lookupByNames(Iterable<String> names) {
		final Map<String,Optional<GameProfile>> map = new HashMap<String,Optional<GameProfile>>();
		for (String name : names) {
			map.put(name, this.lookupByName(name));
		}

		return map;
	}

	@Override
	public Optional<GameProfile> getOrLookupByName(String name) {
		return this.get(name, Mode.ALL);
	}

	@Override
	public Map<String, Optional<GameProfile>> getOrLookupByNames(Iterable<String> names) {
		final Map<String,Optional<GameProfile>> map = new HashMap<String,Optional<GameProfile>>();
		for (String name : names) {
			map.put(name, this.getOrLookupByName(name));
		}

		return map;
	}

	@Override
	public Optional<GameProfile> fillProfile(GameProfile profile, boolean signed) {
		return this.getOrLookupById(profile.getUniqueId());
	}

	@Override
	public Collection<GameProfile> getProfiles() {
		return this.cache.values();
	}

	@Override
	public Collection<GameProfile> match(String name) {
		Collection<GameProfile> list = new ArrayList<GameProfile>();
		for (PlayerData pd : UUIDProvider.getInstance().getCachedPlayersData().values()) {
			if (pd.getName() != null && pd.getName().startsWith(name)) {
				list.add(this.getById(pd.getUUID()).get());
			}
		}
		return list;
	}

	public Optional<GameProfile> get(UUID uniqueId, int mode) {
		GameProfile profile = this.cache.get(uniqueId);
		if (profile != null) {
			return Optional.of(profile);
		}

		final String name = UUIDProvider.get(uniqueId);
		if (name == null) {
			return Optional.empty();
		}

		profile = GameProfile.of(uniqueId, name);
		this.cache.put(uniqueId, profile);
		return Optional.of(profile);
	}

	public Optional<GameProfile> get(String name, int mode) {
		final UUID uuid = UUIDProvider.get(name);
		if (uuid == null) {
			return Optional.empty();
		}
		return this.get(uuid, mode);
	}
}
