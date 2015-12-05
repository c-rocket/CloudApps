package com.oracle.iot.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public class DeviceGitDao {
	private String repoLocation = "";

	private Map<String, String> devices = new HashMap<String, String>();

	public void setRepoLocation(String repo) {
		repoLocation = repo;
	}

	public Map<String, String> pullDevices() {
		return devices;
	}

}
