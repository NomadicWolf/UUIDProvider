package net.kaikk.mc.uuidprovider.common;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.kaikk.mc.kaiscommons.CommonUtils;
import net.kaikk.mc.kaiscommons.mysql.AMySQLQueries;
import net.kaikk.mc.kaiscommons.mysql.MySQLConnection;

public class MySQLQueries extends AMySQLQueries {
	private PreparedStatement createDatabase, clearDatabase, deleteExpiredEntries, getCachedData, addCachedData, getCachedDataByName, getCachedDataById, removeCachedData;

	@Override
	protected void init(MySQLConnection<? extends AMySQLQueries> connection) throws SQLException {
		this.createDatabase = connection.prepareStatement("CREATE TABLE IF NOT EXISTS uuidcache ("
				+ "  uuid binary(16) NOT NULL,"
				+ "  name char(16) NOT NULL,"
				+ "  lastcheck int(11) NOT NULL,"
				+ "  PRIMARY KEY (uuid),"
				+ "  KEY name (name));");
		this.clearDatabase = connection.prepareStatement("TRUNCATE uuidcache");

		this.deleteExpiredEntries = connection.prepareStatement("DELETE FROM uuidcache WHERE lastcheck < ?");
		this.getCachedData = connection.prepareStatement("SELECT * FROM uuidcache");
		this.addCachedData = connection.prepareStatement("INSERT INTO uuidcache VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE uuid = VALUES(uuid), name = VALUES(name), lastcheck = VALUES(lastcheck)");
		this.getCachedDataByName = connection.prepareStatement("SELECT * FROM uuidcache WHERE name = ? AND lastcheck > ? LIMIT 1");
		this.getCachedDataById = connection.prepareStatement("SELECT * FROM uuidcache WHERE uuid = ? AND lastcheck > ? LIMIT 1");
		this.removeCachedData = connection.prepareStatement("DELETE FROM uuidcache WHERE uuid = ? LIMIT 1");
	}

	public void createDatabase() throws SQLException {
		this.createDatabase.executeUpdate();
	}

	public void clearCache() throws SQLException {
		this.clearDatabase.executeUpdate();
	}

	public int deleteExpiredEntries() throws SQLException {
		this.deleteExpiredEntries.setInt(1, CommonUtils.epoch()-7776000);
		return this.deleteExpiredEntries.executeUpdate();
	}

	public ResultSet getCachedData() throws SQLException {
		return this.getCachedData.executeQuery();
	}

	public void addCachedData(UUID uuid, String name, int lastCheck) throws SQLException {
		this.addCachedData.setBytes(1, CommonUtils.UUIDtoByteArray(uuid));
		this.addCachedData.setString(2, name);
		this.addCachedData.setInt(3, lastCheck);
		this.addCachedData.executeUpdate();
	}

	public ResultSet getCachedData(String name) throws SQLException {
		this.getCachedDataByName.setString(1, name);
		this.getCachedDataByName.setInt(2, CommonUtils.epoch()-3196800);
		return this.getCachedDataByName.executeQuery();
	}

	public ResultSet getCachedData(UUID uuid) throws SQLException {
		this.getCachedDataById.setBytes(1, CommonUtils.UUIDtoByteArray(uuid));
		this.getCachedDataById.setInt(2, CommonUtils.epoch()-3196800);
		return this.getCachedDataById.executeQuery();
	}

	public void removeCachedData(UUID uuid) throws SQLException {
		this.removeCachedData.setBytes(1, CommonUtils.UUIDtoByteArray(uuid));
		this.removeCachedData.executeUpdate();
	}
}
