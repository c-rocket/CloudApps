package com.oracle.iot.dao;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Repository
public class DeviceGitDao {
	private String repoLocation = "";

	private Map<String, String> devices = new HashMap<String, String>();

	public void setRepoLocation(String repo) {
		repoLocation = repo;
	}

	public Map<String, String> pullDevices() {
		try {
			// File gitWorkDir = new File(repoLocation);
			// Git git = Git.open(gitWorkDir);
			// Repository repo = git.getRepository();
		} catch (Exception e) {

		}
		return devices;
	}

}
