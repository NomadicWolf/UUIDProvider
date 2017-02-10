package net.kaikk.mc.uuidprovider;

import java.sql.SQLException;

import javax.sql.DataSource;

public abstract class Config {
	public String dbHostname, dbUsername, dbPassword, dbDatabase;
	public boolean offlineMode, dummyMode;

	public abstract DataSource getDataSource() throws SQLException;
}
