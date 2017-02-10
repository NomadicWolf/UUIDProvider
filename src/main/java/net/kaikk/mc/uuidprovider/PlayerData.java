package net.kaikk.mc.uuidprovider;

import java.util.UUID;

import net.kaikk.mc.kaiscommons.CommonUtils;

public class PlayerData {
	private UUID uuid;
	private String name;
	private int lastCheck;

	public PlayerData(UUID uuid, String name) {
		this(uuid, name, CommonUtils.epoch());
	}

	public PlayerData(UUID uuid, String name, int lastCheck) {
		this.uuid = uuid;
		this.name = name;
		this.lastCheck = lastCheck;
	}

	public boolean check() {
		if (this.name==null||this.uuid==null) {
			return (CommonUtils.epoch()-this.lastCheck < 10);
		}

		return (CommonUtils.epoch()-this.lastCheck < 3196800); // 37 days
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public int getLastCheck() {
		return lastCheck;
	}
}
