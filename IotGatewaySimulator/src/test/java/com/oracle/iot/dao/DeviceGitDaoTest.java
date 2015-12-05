package com.oracle.iot.dao;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class DeviceGitDaoTest {

	DeviceGitDao dao = new DeviceGitDao();

	@Before
	public void setUp() {
		String repo = "C:\\Users\\ccrocket\\git\\devicesimulator";
		dao.setRepoLocation(repo);
	}

	@Test
	public void pullDevices() throws Exception {
		// execute
		Map<String, String> devices = dao.pullDevices();

		// assert
		assertTrue(true);
	}
}
