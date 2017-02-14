package net.kaikk.mc.uuidprovider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Iterables;

import net.kaikk.mc.kaiscommons.CommonUtils;
import net.kaikk.mc.kaiscommons.mysql.MySQLConnection;
import net.kaikk.mc.uuidprovider.common.MySQLQueries;

public class UUIDProvider {
	private static UUIDProvider instance;

	private Map<CIString,PlayerData> cachedPlayersName = new ConcurrentHashMap<CIString,PlayerData>();
	private Map<UUID,PlayerData> cachedPlayersUUID = new ConcurrentHashMap<UUID,PlayerData>();

	private MySQLConnection<MySQLQueries> conn;
	private Config config;

	public UUIDProvider(Config config) throws SQLException {
		this.config = config;
		this.conn = new MySQLConnection<>(config.getDataSource(), MySQLQueries.class);

		try {
			this.conn.queries().createDatabase();

			// load cached data from database
			ResultSet results = this.conn.queries().getCachedData();
			while(results.next()) {
				PlayerData playerData = new PlayerData(CommonUtils.toUUID(results.getBytes(1)), results.getString(2), results.getInt(3));
				this.cachedPlayersUUID.put(playerData.getUUID(), playerData);
				this.cachedPlayersName.put(new CIString(playerData.getName()), playerData);
			}
		} finally {
			this.conn.close();
		}
	}

	public static void init(Config config) throws SQLException {
		instance = new UUIDProvider(config);
	}

	/**
	 * Get the UUID of the specified name, using all available modes <br>
	 * Thread-safe
	 * @param name the player's name
	 * @return the player's uuid, null if not found
	 */
	public static UUID get(String name) {
		return get(name, Mode.ALL);
	}


	/**
	 * Get the UUID of the specified name, using the specified mode<br>
	 * One ore more modes can be specified, delimited by the | operator.<br>
	 * Example: UUID uuid = get(name, Mode.INTERNAL | Mode.DATABASE); <br>
	 * Thread-safe
	 * @param name the player name
	 * @param mode the selected modes to get the uuid
	 * @return the player's uuid, null if not found
	 */
	public static UUID get(String name, int mode) {
		if (name.length()>16) {
			return null;
		}

		if (!name.matches("[a-zA-Z0-9_]+")) {
			return null;
		}

		Mode m = Mode.from(mode);

		if (m.check(Mode.INTERNAL)) {
			// Internal cache
			PlayerData playerData = getFromInternalCache(name);
			if (playerData != null) {
				return playerData.getUUID();
			}
		}

		if (m.check(Mode.DATABASE)) {
			// MySQL database
			try {
				PlayerData playerData = instance.getPlayerData(name);
				if (playerData != null) {
					cache(playerData.getUUID(), name);
					return playerData.getUUID();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		UUID uuid = null;

		if (m.check(Mode.MOJANG)) {
			// If offline-mode is on, do not request an UUID to Mojang, but generate a new UUID
			if (instance.config.offlineMode) {
				uuid = UUID.randomUUID();
			} else {
				// Request to Mojang
				UUIDFetcher uuidfetcher = new UUIDFetcher(Arrays.asList(name));
				try {
					Map<String,UUID> nameToUUIDMap = uuidfetcher.call();
					uuid=Iterables.getFirst(nameToUUIDMap.values(), null);
				} catch (Exception e) { }
			}
			cache(uuid, name);
		}

		return uuid;
	}


	/**
	 * Get a map of uuids from the specified names list, using all available modes<br>
	 * This is the best method if you have to get a lot of uuids at the same time<br>
	 * Thread-safe
	 * @param names a collection of players' name
	 * @return a map of player's name - uuid. if the uuid of a player cannot be found, it won't be included into the returned map.
	 */
	public static Map<String,UUID> getUUIDs(Collection<String> names) {
		return getUUIDs(names, Mode.ALL);
	}


	/**
	 * Get a map of uuids from the specified names list, using the specified mode<br>
	 * This is the best method if you have to get a lot of uuids at the same time<br>
	 * One ore more modes can be specified, delimited by the | operator.<br>
	 * Example: UUID uuid = get(names, Mode.INTERNAL | Mode.DATABASE); <br>
	 * Thread-safe
	 * @param names a collection of players' name
	 * @param mode the selected modes to get the uuid
	 * @return a map of player's name - uuid. if the uuid of a player cannot be found, it won't be included into the returned map.
	 */
	public static Map<String,UUID> getUUIDs(Collection<String> names, int mode) {
		Map<String,UUID> map = new HashMap<String,UUID>(names.size());

		if (Mode.check(Mode.MOJANG, mode)) {
			mode -= Mode.MOJANG;

			List<String> namesToRequest = new ArrayList<String>(names.size());
			for (String name : names) {
				if (name.matches("[a-zA-Z0-9_]+")) {
					UUID uuid = get(name, mode);
					if (uuid==null) {
						namesToRequest.add(name);
					}
					map.put(name, uuid);
				}
			}

			if (!namesToRequest.isEmpty()) {
				// Request to Mojang
				UUIDFetcher fetcher = new UUIDFetcher(namesToRequest);
				try {
					Map<String,UUID> map2 = fetcher.call();
					map.putAll(map2);

					// cache results
					for (Entry<String,UUID> entry : map2.entrySet()) {
						cache(entry.getValue(), entry.getKey());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			for (String name : names) {
				UUID uuid = get(name, mode);
				map.put(name, uuid);
			}
		}

		return map;
	}

	public static String get(UUID uuid) {
		return get(uuid, Mode.ALL);
	}

	public static String get(UUID uuid, int mode) {
		Mode m = Mode.from(mode);

		if (m.check(Mode.INTERNAL)) {
			// Internal cache
			PlayerData playerData = getFromInternalCache(uuid);
			if (playerData != null) {
				return playerData.getName();
			}
		}

		String name = null;

		if (m.check(Mode.DATABASE)) {
			// MySQL database
			try {
				PlayerData playerData = instance.getPlayerData(uuid);
				if (playerData != null) {
					cache(playerData.getUUID(), name);
					return playerData.getName();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (m.check(Mode.MOJANG)) {
			// If offline-mode is on, do not request an UUID to Mojang
			if (!instance.config.offlineMode) {
				// Request to Mojang
				NameFetcher namefetcher = new NameFetcher(Arrays.asList(uuid));
				try {
					Map<UUID,String> UUIDToNameMap = namefetcher.call();
					name=Iterables.getFirst(UUIDToNameMap.values(), null);
				} catch (Exception e) { }
			}
		}

		cache(uuid, name);
		return name;
	}

	public static Map<UUID,String> getNames(Collection<UUID> uuids) {
		return getNames(uuids, Mode.ALL);
	}

	public static Map<UUID,String> getNames(Collection<UUID> uuids, int mode) {
		final Map<UUID,String> map = new HashMap<UUID,String>(uuids.size());

		if (Mode.check(Mode.MOJANG, mode)) {
			mode -= Mode.MOJANG;

			List<UUID> uuidsToRequest = new ArrayList<UUID>(uuids.size());
			for (UUID uuid : uuids) {
				String name = get(uuid, mode);
				if (uuid==null) {
					uuidsToRequest.add(uuid);
				}
				map.put(uuid, name);
			}

			if (!uuidsToRequest.isEmpty()) {
				// Request to Mojang
				NameFetcher fetcher = new NameFetcher(uuidsToRequest);
				try {
					Map<UUID,String> map2 = fetcher.call();
					map.putAll(map2);

					// cache results
					for(Entry<UUID,String> entry : map2.entrySet()) {
						cache(entry.getKey(), entry.getValue());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			for (UUID uuid : uuids) {
				String name = get(uuid, mode);
				map.put(uuid, name);
			}
		}

		return map;
	}

	public static void cache(UUID uuid, String name) {
		final PlayerData playerData = new PlayerData(uuid, name);
		if (name!=null) {
			instance.cachedPlayersName.put(new CIString(name), playerData);
		}

		if (uuid!=null) {
			PlayerData oldPlayerData;
			if ((oldPlayerData=instance.cachedPlayersUUID.put(uuid, playerData))!=null && oldPlayerData.getName()!=null) {
				instance.cachedPlayersName.remove(oldPlayerData.getName()); // this player changed name... remove old name from cache
			}

			if (name!=null) {
				try {
					instance.addData(playerData);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void cacheInternal(UUID uuid, String name) {
		if (uuid != null && name!=null) {
			final PlayerData playerData = new PlayerData(uuid, name);
			instance.cachedPlayersName.put(new CIString(name), playerData);
			PlayerData oldPlayerData;
			if ((oldPlayerData=instance.cachedPlayersUUID.put(uuid, playerData))!=null && oldPlayerData.getName()!=null) {
				instance.cachedPlayersName.remove(oldPlayerData.getName()); // this player changed name... remove old name from cache
			}
		}
	}

	private static PlayerData getFromInternalCache(UUID uuid) {
		final PlayerData playerData = instance.cachedPlayersUUID.get(uuid);
		if (playerData!=null && playerData.check()) {
			return playerData;
		}
		return null;
	}

	private static PlayerData getFromInternalCache(String name) {
		final PlayerData playerData = instance.cachedPlayersName.get(new CIString(name));
		if (playerData!=null && playerData.check()) {
			return playerData;
		}
		return null;
	}

	public MySQLConnection<MySQLQueries> getConnection() {
		return conn;
	}

	public Config getConfig() {
		return config;
	}

	public static UUIDProvider getInstance() {
		return instance;
	}

	public Map<UUID, PlayerData> getCachedPlayersData() {
		return Collections.unmodifiableMap(cachedPlayersUUID);
	}

	public static void clearCache() throws SQLException {
		instance.cachedPlayersName.clear();
		instance.cachedPlayersUUID.clear();
		try {
			instance.conn.queries().clearCache();
		} finally {
			instance.conn.close();
		}
	}

	public static void removeData(UUID uuid) throws SQLException {
		PlayerData pd = instance.cachedPlayersUUID.remove(uuid);
		if (pd == null) {
			return;
		}
		instance.cachedPlayersName.remove(pd.getName().toLowerCase());
		try {
			instance.conn.queries().removeCachedData(uuid);
		} finally {
			instance.conn.close();
		}
	}

	private void addData(PlayerData playerData) throws SQLException {
		try {
			this.conn.queries().addCachedData(playerData.getUUID(), playerData.getName(), playerData.getLastCheck());
		} finally {
			this.conn.close();
		}
	}

	private PlayerData getPlayerData(String name) throws SQLException {
		try {
			ResultSet rs = this.conn.queries().getCachedData(name);
			if (rs.next()) {
				PlayerData playerData = new PlayerData(CommonUtils.toUUID(rs.getBytes(1)), rs.getString(2), rs.getInt(3));
				// cache result
				this.cachedPlayersUUID.put(playerData.getUUID(), playerData);
				this.cachedPlayersName.put(new CIString(playerData.getName()), playerData);
				return playerData;
			}
		} finally {
			this.conn.close();
		}
		return null;
	}

	private PlayerData getPlayerData(UUID uuid) throws SQLException {
		try {
			ResultSet rs = this.conn.queries().getCachedData(uuid);
			if (rs.next()) {
				PlayerData playerData = new PlayerData(CommonUtils.toUUID(rs.getBytes(1)), rs.getString(2), rs.getInt(3));
				// cache result
				this.cachedPlayersUUID.put(playerData.getUUID(), playerData);
				this.cachedPlayersName.put(new CIString(playerData.getName()), playerData);
				return playerData;
			}
		} finally {
			this.conn.close();
		}
		return null;
	}


	/**
	 * Modes used in get methods (see get method for more informations how to use this class)<br>
	 *  INTERNAL: Check on the internal map only <br>
	 *  DATABASE: Check the MySQL database<br>
	 *  MOJANG: Send a request to the Mojang (very slow!) <br>
	 *  ALL: All the above, in that order.
	 */
	public static class Mode {
		public final static int INTERNAL = 1;
		public final static int DATABASE = 2;
		public final static int MOJANG = 4;
		public final static int ALL = 7;

		private int value;

		private Mode() { }

		private Mode(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public boolean check(int mode) {
			return (this.value & mode) != 0;
		}

		public static Mode from(int mode) {
			return new Mode(mode);
		}

		public static boolean check(int mode, int selectedModes) {
			return (mode & selectedModes) != 0;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Mode)) {
				return false;
			}
			Mode other = (Mode) obj;
			if (value != other.value) {
				return false;
			}
			return true;
		}


	}
}
